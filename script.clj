#!/usr/bin/env bb

(ns script
      (:require [babashka.cli :as cli]
                [clj-yaml.core :as yaml]
                [clojure.java.io :as io]
                [clojure.string :as c-str]
                [clojure.java.shell :as shell]
                [clojure.data.json :as json]))

;; ================================== // Config // ==================================

(def cli_version "1.0.0")
(def file-num-re #"\((\d+)\)")

(def file-setup
  {:setup
   {:images ["png" "jpeg" "jpg" "svg" "webp" "ico" "drawio"]
    :PowerPoints ["pptx" "ppt"]
    :Spreadsheets ["xlsx" "xls" "csv"]
    :Documents ["docx" "doc" "pdf" "txt" "md" "html"]
    :ArchiveFiles ["zip" "rar" "7z"]
    :Videos ["mp4" "gif"]
    :Music ["mp3"]
    :LaTex ["tex"]
    :Executable ["exe" "msi"]
    :Programming ["py" "json" "db" "yml" "yaml"]
    :Outlook ["msg"]}})

(defn get-root-dir []
  (.getAbsolutePath (io/file ".") ))

(defn join-path [& components]
  (apply io/file components))

(defn get-user-home-dir []
  (System/getProperty "user.home"))

(defn get-downloads-dir []
  (-> (get-user-home-dir)
      (join-path "Downloads")))

(defn get-setup-dir []
  (join-path (get-root-dir) "setup.yml"))

(def cli-config
  {:input-folder {:desc "The input folder directory"
                  :require false
                  :alias :i
                  :default (str (get-downloads-dir))
                  :type :str}
   :setup {:desc "The path to the setup yml file"
           :require false
           :alias :s
           :default (str (get-setup-dir))
           :type :str}
   :generate-setup {:desc "Generates a default setup.yml file"
                    :require false
                    :alias :g
                    :default false
                    :type :boolean}
   :help {:desc "Show help"
          :require false
          :alias :h
          :type :boolean}
   :version {:desc "Show version"
             :require false
             :alias :v
             :type :boolean}})

;; ================================== // Utils // ==================================

(defn generate-setup-file
  [output-path]
  (spit
    (io/file output-path)
    (yaml/generate-string
      file-setup
      :dumper-options {:indent 4
                       :indicator-indent 2
                       :flow-style :block})))

(defn get-name-and-ext [file]
  (let [name (.getName (io/file file))
        [_ base ext] (re-find #"(.+?)(?:\.([^.]+))?$" name)]
    {:name base
     :ext ext}))

(defn next-file-num
  [file]
  (let [[_ file-num] (re-find file-num-re (str file))]
    (if file-num
      (inc (Integer/parseInt file-num))
      1)))

(defn generate-new-file-name
  [file name ext]
  (let [new-num (next-file-num file)]
    (if (> new-num 1)
      (format "%s (%d).%s" (c-str/replace name file-num-re "") new-num ext)
      (format "%s (%d).%s" name new-num ext))))

(defn safe-new-file
  [file]
  (let [{:keys [name ext]} (get-name-and-ext file)]
    (loop [new-file (io/file file)]
      (if (.exists new-file)
        (recur (io/file (.getParent new-file)
                        (generate-new-file-name new-file name ext)))
        new-file))))

(defn shell-mv
  [from to]
  (try
    ;; Use the shell to move the file
    (let [{:keys [exit err]} (shell/sh "mv" from (str to))]
      (if (zero? exit)
        (println (format "%s moved successfully" to))
        (println "Error moving file:" err)))
    (catch Exception e
      (println "Error executing move:" (.getMessage e)))))

(defn safe-move
  [input-file-path target-dir]
  (let [input-file-name (.getName (io/file input-file-path))
        output-file-dir (->> (join-path target-dir input-file-name)
                             safe-new-file)]
    (if (.exists output-file-dir)
      (println "File already exists in target directory. Skipping.")
      (shell-mv input-file-path output-file-dir))))


(defn list-files
  "Thank you: https://clojuredocs.org/clojure.core/file-seq"
  [root-dir file-types]
  (let [grammar-matcher (.getPathMatcher
                          (java.nio.file.FileSystems/getDefault)
                          (format "glob:*.{%s}"
                                  (c-str/join
                                    ","
                                    (map #(c-str/lower-case %) file-types))))]

    (->> (io/file root-dir)                        ;; Use io/file to get the root-dir
         .listFiles                                 ;; file-seq goes DEEP!
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %)))))

(defn get-setup
  [setup-dir]
  (when-not (.exists (io/file setup-dir))
    (println (format "The setup file '%s' does not exist" setup-dir))
    (System/exit 1))

  (let [yml-setup (yaml/parse-string (slurp (io/file setup-dir)))]
    (:setup yml-setup)))

;; ================================== // Main // ==================================

;; Main function
(defn main [{:keys [input-folder setup generate-setup]}]
  (when generate-setup
    (println "Generating setup.yml")
    (generate-setup-file setup)
    (System/exit 0))

  (doseq [[output-folder file-types] (get-setup (get-setup-dir))]
    (let [input-file-paths (list-files input-folder file-types)]
      (doseq [input-file-path input-file-paths]
        (let [output-folder-dir (join-path input-folder (name output-folder))]
          (when-not (.exists output-folder-dir)
            (.mkdirs output-folder-dir))
          (safe-move input-file-path output-folder-dir)))))

)

;; ====================== // Utils to process args// ======================

(defn print-help
  "Prints the help"
  [cli-config]
  (println "Usage: ./<filename> <options>")
  (println (str "Version: " cli_version))
  (println (cli/format-opts {:spec cli-config})))

(defn cli-err-handler
  [{:keys [spec type cause msg option opts] :as data}]
  ;; only handle cli errors
  ;(println data)
  (if (= :org.babashka/cli type)
    (case cause
      ;; Handle help and version requests without errors
      :require
      (when-not (or (:help opts) (:version opts))
        (println
          (format "Missing required argument:\n%s"
                  (cli/format-opts {:spec (select-keys spec [option])})))
        (System/exit 1))
      ;; case: default ------
      (println "Default err message:")
      (println msg))
    (throw (ex-info msg data))))


;; ================================== // Process Args // ==================================

(let [cli-args (cli/parse-opts *command-line-args* {:spec cli-config :error-fn cli-err-handler})]  ;; Apply alias remapping
  ;(println cli-args)
  (cond
    (:help cli-args)    (print-help cli-config)
    (:version cli-args) (println "Version:" cli_version)
    :else               (main cli-args)))


;; ================================== // Development // ==================================

(comment


      ())
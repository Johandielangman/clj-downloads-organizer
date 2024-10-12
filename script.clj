#!/usr/bin/env bb

(ns script
      (:require [babashka.cli :as cli]
                [clj-yaml.core :as yaml]
                [clojure.java.io :as io]
                [clojure.string :as c-str]
                [clojure.data.json :as json]))

;; ================================== // Config // ==================================

(def cli_version "1.0.0")
(def file-num-re #"\((\d+)\)")

(def cli-config
      {:name {
              :desc "Your name"
              :require true
              :alias :n
              :type :str}
       :age {
             :desc "Your age"
             :require false
             :alias :a
             :type :int}
       :help {
              :desc "Show help"
              :require false
              :alias :h
              :type :boolean}
       :version {
                 :desc "Show version"
                 :require false
                 :alias :v
                 :type :boolean}})

;; ================================== // Utils // ==================================

(defn get-root-dir []
  (.getAbsolutePath (io/file ".") ))

(defn join-path [& components]
  (apply io/file components))

(defn get-user-home-dir []
  (System/getProperty "user.home"))

(defn get-downloads-dir []
  (-> (get-user-home-dir)
      (join-path "Downloads")))

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

(defn safe-move
  [input-file-path target-dir]
  (let [input-file-name (.getName input-file-path)
        output-file-dir (->> (join-path target-dir input-file-name)
                             safe-new-file)]
    (if (.exists output-file-dir)
      (println "File already exists in target directory. Skipping.")
      (try
        (io/copy input-file-path output-file-dir)
        (io/delete-file input-file-path)
        (println (format "%s moved successfully" (.getName output-file-dir)))
        (catch Exception e
          (println "Error moving file:" (.getMessage e)))))))


(defn list-files
  "Thank you: https://clojuredocs.org/clojure.core/file-seq"
  [root-dir & file-types]
  (let [grammar-matcher (.getPathMatcher
                          (java.nio.file.FileSystems/getDefault)
                          (format "glob:*.{%s}"
                                  (c-str/join
                                    ","
                                    (map #(c-str/lower-case %) file-types))))]
    (->> root-dir
         io/file
         .listFiles                                         ;; file-seq goes DEEP!
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %)))))

;; ================================== // Main // ==================================

;; Main function
(defn main [{:keys [name age verbose]}]
      (println "Hello from Babashka!")
      (println "Received name:" name)
      (when age
            (println "Received age:" age))
      (println "JSON example:" (json/write-str {:name name :age age :verbose verbose})))



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
      (when (not (or (:help opts) (:version opts)))
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
      (def cli-args (cli/parse-opts
                      '("-n" "johan")
                      {:spec cli-config :error-fn cli-err-handler}))
      cli-args

      (print-help cli-config)

      (main cli-args)

      ;; https://github.com/clj-commons/clj-yaml/blob/master/doc/01-user-guide.adoc
      (def file-setup {:setup
                       {:documents ["pdf"]
                        :images ["png" "jpg" "jpeg"]}})
      (yaml/generate-string
        file-setup
        :dumper-options {:indent 4
                         :flow-style :block})

      (spit
        "./setup.yml"
        (yaml/generate-string
          file-setup
          :dumper-options {:indent 4
                           :indicator-indent 2
                           :flow-style :block}))

      (def setup
        (yaml/parse-string (slurp "./setup.yml")))
      setup
      (:setup setup)

      (cli/parse-opts '("-n" "johan") {:spec cli-config})

      ;; files and directories ========================================
      (.exists (io/file "setup.yml"))
      (.exists (io/file "i-am-not-real.lol"))
      (.isDirectory (io/file "input"))
      (.getName (io/file "setup.yml"))
      (.getParent  (io/file "./input/dog.csv"))
      (.getPath  (io/file "./input/dog.csv"))
      (.getAbsolutePath  (io/file "./input/dog.csv"))
      (.mkdirs (io/file "./a/b"))
      (file-seq (io/file "./input/"))

      ;; custom
      (str (join-path (get-root-dir) "foo" "bar"))
      (str (join-path (user-home) "Downloads"))

      (list-files (io/file "./input/") "TxT" "pdf")

      (list-files (get-downloads-dir) "TxT" "pdf")



      ;; https://regexr.com/3sqfe
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo.txt")
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo.bar.txt")
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo")
      (get-name-and-ext "boo.txt")

      (re-pattern "\\s*\\((\\d+)\\)")

      (next-file-num "setup.yml")
      (next-file-num "setup (2).txt")
      (next-file-num "file (2).txt")

      (def input-files (list-files (join-path (get-root-dir) "input") "png"))

      (c-str/replace "file (2).txt" file-num-re "(3)")

      (def input-file (io/file (first input-files)))
      (safe-new-file input-file)

      (c-str/replace "dog" file-num-re (next-file-num input-file))

      (safe-move input-file (get-downloads-dir))



      ())
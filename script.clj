#!/usr/bin/env bb

(ns script
      (:require [babashka.cli :as cli]
                [clojure.data.json :as json]))

;; ================================== // Config // ==================================

(def cli_version "1.0.0")

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
  (println "Usage: ./<filename> <args>")
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

      (cli/parse-opts '("-n" "johan") {:spec cli-config})

      ())
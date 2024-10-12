

```clojure

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

      (re-pattern "\\s*\\((\\d+)\\)")

      ;; https://regexr.com/3sqfe
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo.txt")
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo.bar.txt")
      (re-find #"(.+?)(?:\.([^.]+))?$" "foo")
      (get-name-and-ext "boo.txt")

      ())
```
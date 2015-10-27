(defproject uap-clj-hiveudf "1.0.3"
  :description "Apache Hadoop Hive GenericUDF wrapper around uap-clj"
  :url "https://github.com/russellwhitaker/uap-clj-hiveudf"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj-hiveudf"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [uap-clj "1.0.3"]]
  :exclusions [org.apache.hadoop/hadoop-core
               org.apache.hadoop/hadoop-common
               org.apache.hadoop/hadoop-hdfs]
  :profiles {:provided
               {:dependencies
                 [[org.apache.hive/hive-exec "0.13.0"]
                  [org.apache.hive/hive-serde "0.13.0"]
                  [org.apache.hadoop/hadoop-core "1.2.1"]]}
             :dev
               {:dependencies [[speclj "3.3.1"]
                               [lein-git-deps "0.0.2"]]}}
  :plugins [[lein-git-deps "0.0.2"]
            [speclj "3.3.1"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :jvm-opts ["-Xss2m"]
  :test-paths ["spec"]
  :aot :all)

(defproject uap-clj-hiveudf "1.3.0"
  :description "Apache Hadoop Hive GenericUDF wrapper around uap-clj"
  :url "https://github.com/russellwhitaker/uap-clj-hiveudf"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj-hiveudf"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [uap-clj "1.3.0"]]
  :exclusions [org.apache.hadoop/hadoop-core
               org.apache.hadoop/hadoop-common
               org.apache.hadoop/hadoop-hdfs]
  :profiles {:provided
               {:dependencies
                 [[org.apache.hive/hive-exec "2.1.1"]
                  [org.apache.hive/hive-serde "2.1.1"]
                  [org.apache.hadoop/hadoop-core "1.2.1"]]}
             :dev
               {:dependencies [[speclj "3.3.2"]
                               [lein-git-deps "0.0.2"]]
                :test-paths ["spec"]}}
  :plugins [[lein-git-deps   "0.0.2"]
            [lein-ancient    "0.6.10"]
            [lein-bikeshed   "0.4.1"]
            [jonase/eastwood "0.2.3"]
            [speclj          "3.3.2"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :jvm-opts ["-Xss2m"]
  :aot :all
  :aliases {"test"  ["do" ["clean"] ["spec" "--reporter=d"]]
            "build" ["do" ["clean"] ["uberjar"]]})

(defproject uap-clj-hiveudf "1.0.0"
  :description "Apache Hadoop Hive SimpleUDF wrapper around uap-clj"
  :url "https://github.com/russellwhitaker/uap-clj-hiveudf"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj-hiveudf"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [uap-clj "1.0.0"]]
  :profiles {:provided
               {:dependencies
                 [[org.apache.hive/hive-exec "0.12.0"]
                 [org.apache.hive/hive-serde "0.12.0"]
                 [org.apache.hadoop/hadoop-core "1.2.1"]]}
             :dev
               {:dependencies [[speclj "3.2.0"]
                               [lein-git-deps "0.0.2-SNAPSHOT"]]}}
  :plugins [[lein-git-deps "0.0.2-SNAPSHOT"]
            [speclj "3.2.0"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :jvm-opts ["-Xss2m"]
  :test-paths ["spec"]
  :aot :all)

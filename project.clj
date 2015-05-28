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
                 [org.apache.hadoop/hadoop-core "1.2.1"]]}}
  :aot :all)

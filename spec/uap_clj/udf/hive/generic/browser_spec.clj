(ns uap-clj.udf.hive.generic.browser-spec
  (:import [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF$DeferredObject
                                                  GenericUDF$DeferredJavaObject])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.common-spec :refer [unknown-useragent]]
            [uap-clj.udf.hive.generic.browser :refer [-evaluate]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def tests-browser (:test_cases
                     (parse-string
                       (slurp (io/resource "tests/test_ua.yaml")))))

(defn run-browser-fixture
  "Assert match between ua-parser/ua-core fixture test data:
     Browser family/major/minor/patch
   and output of useragent parser GenericUDF Browser function
  "
  [fixture]
  (let [browser-parser (uap-clj.udf.hive.generic.Browser.)
        ua (:user_agent_string fixture)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject. ua)])
        result (.evaluate browser-parser deferred-args)
        expected (select-keys fixture [:family :major :minor :patch])
        [actual-family actual-major actual-minor actual-patch]
          (map #(.toString %) (into-array Text result))]
  (do-template [expected-family expected-major expected-minor expected-patch]
               (describe (format "a user agent '%s'" ua)
                 (it (format "is in the '%s' browser family" expected-family)
                   (should= expected-family actual-family))
                 (it (format "has '%s' as its major number" expected-major)
                   (should= expected-major actual-major))
                 (it (format "has '%s' as its minor number" expected-minor)
                   (should= expected-minor actual-minor))
                 (it (format "has '%s' as its patch number" expected-patch)
                   (should= expected-patch actual-patch)))
               (str (get expected :family ""))
               (str (get expected :major ""))
               (str (get expected :minor ""))
               (str (get expected :patch "")))))

(context "Known Browsers:"
  (map #(run-browser-fixture %) tests-browser))

(context "Unknown Browser:"
  (let [browser-parser (uap-clj.udf.hive.generic.Browser.)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject.
                                     unknown-useragent)])
        result (.evaluate browser-parser deferred-args)
        [expected-family expected-major expected-minor expected-patch]
          ["Other" "" "" ""]
        [actual-family actual-major actual-minor actual-patch]
          (map #(.toString %) (into-array Text result))]
    (describe (format "A user agent '%s'" unknown-useragent)
      (it (format "is in the '%s' browser family" expected-family)
        (should= expected-family actual-family))
      (it (format "has '%s' as its major number" expected-major)
        (should= expected-major actual-major))
      (it (format "has '%s' as its minor number" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "has '%s' as its patch number" expected-patch)
        (should= expected-patch actual-patch)))))

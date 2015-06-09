(ns uap-clj.udf.hive.generic.os-spec
  (:import [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF$DeferredObject
                                                  GenericUDF$DeferredJavaObject])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.common-spec :refer [unknown-useragent]]
            [uap-clj.udf.hive.generic.os :refer [-evaluate]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def tests-os (:test_cases
                     (parse-string
                       (slurp (io/resource "tests/test_os.yaml")))))

(defn run-os-fixture
  "Assert match between ua-parser/ua-core fixture test data:
     O/S family/major/minor/patch/patch_minor
   and output of useragent parser GenericUDF OS function
  "
  [fixture]
  (let [os-parser (uap-clj.udf.hive.generic.OS.)
        ua (:user_agent_string fixture)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject. ua)])
        result (.evaluate os-parser deferred-args)
        expected (select-keys fixture [:family :major :minor :patch :patch_minor])
        [actual-family actual-major actual-minor actual-patch actual-patch-minor]
          (map #(.toString %) (into-array Text result))]
  (do-template [expected-family expected-major expected-minor expected-patch expected-patch-minor]
               (describe (format "a user agent '%s'" ua)
                 (it (format "is in the '%s' o/s family" expected-family)
                   (should= expected-family actual-family))
                 (it (format "has '%s' as its major number" expected-major)
                   (should= expected-major actual-major))
                 (it (format "has '%s' as its minor number" expected-minor)
                   (should= expected-minor actual-minor))
                 (it (format "has '%s' as its patch number" expected-patch)
                   (should= expected-patch-minor actual-patch-minor))
                 (it (format "has '%s' as its patch_minor number" expected-patch-minor)
                   (should= expected-patch-minor actual-patch-minor)))
               (str (get expected :family ""))
               (str (get expected :major ""))
               (str (get expected :minor ""))
               (str (get expected :patch ""))
               (str (get expected :patch_minor "")))))

(context "Known O/S:"
  (map #(run-os-fixture %) tests-os))

(context "Unknown O/S:"
  (let [os-parser (uap-clj.udf.hive.generic.OS.)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject.
                                     unknown-useragent)])
        result (.evaluate os-parser deferred-args)
        [expected-family expected-major expected-minor expected-patch expected-patch-minor]
          ["Other" "" "" "" ""]
        [actual-family actual-major actual-minor actual-patch actual-patch-minor]
          (map #(.toString %) (into-array Text result))]
    (describe (format "A user agent '%s'" unknown-useragent)
      (it (format "is in the '%s' o/s family" expected-family)
        (should= expected-family actual-family))
      (it (format "has '%s' as its major number" expected-major)
        (should= expected-major actual-major))
      (it (format "has '%s' as its minor number" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "has '%s' as its patch number" expected-patch)
        (should= expected-patch actual-patch))
      (it (format "has '%s' as its patch_minor number" expected-patch-minor)
        (should= expected-patch actual-patch-minor)))))

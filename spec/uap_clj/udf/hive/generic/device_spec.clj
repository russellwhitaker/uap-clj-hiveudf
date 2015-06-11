(ns uap-clj.udf.hive.generic.device-spec
  (:import [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF$DeferredObject
                                                  GenericUDF$DeferredJavaObject])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.common-spec :refer [unknown-useragent]]
            [uap-clj.udf.hive.generic.device :refer [-evaluate]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def all-tests-device (:test_cases
                    (parse-string
                      (slurp (io/resource "tests/test_device.yaml")))))
(def partitioned-tests-device (partition 4000 4000 [nil] all-tests-device))

(defn run-device-fixture
  "Assert match between ua-parser/ua-core fixture test data:
     Device family/brand/model
   and output of useragent parser GenericUDF Device function
  "
  [fixture]
  (let [device-parser (uap-clj.udf.hive.generic.Device.)
        ua (:user_agent_string fixture)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject. ua)])
        result (.evaluate device-parser deferred-args)
        expected (select-keys fixture [:family :brand :model])
        [actual-family actual-brand actual-model]
          (map #(.toString %) (into-array Text result))]
  (do-template [expected-family expected-brand expected-model]
               (describe (format "a user agent '%s'" ua)
                 (it (format "is in the '%s' device family" expected-family)
                   (should= expected-family actual-family))
                 (it (format "has '%s' as its brand" expected-brand)
                   (should= expected-brand actual-brand))
                 (it (format "has '%s' as its model" expected-model)
                   (should= expected-model actual-model)))
               (str (get expected :family ""))
               (str (get expected :brand ""))
               (str (get expected :model "")))))

;;;
;;; As of this commit, there are a very large number of tests in test_device.yaml:
;;;   uap-clj.core=> (count tests-device)
;;;   15948
;;; These test fixtures are generated at compile time; exceeding around 4200 assertions
;;;   blows the stack of the thread running the tests in a context below (on the
;;;   developer's machine), but partitioning as below works around that annoyance.
;;;   With this in mind, I've set this in project.clj:
;;;     :jvm-opts ["-Xss2m"]
;;;
(context "Known Devices:"
  (context "Part 1 of 4:"
    (map #(run-device-fixture %)
         (first partitioned-tests-device)))
  (context "Part 2 of 4:"
    (map #(run-device-fixture %)
         (second partitioned-tests-device)))
  (context "Part 3 of 4:"
    (map #(run-device-fixture %)
         (nth partitioned-tests-device 2)))
  (context "Part 4 of 4:"
    (map #(run-device-fixture %)
         (butlast (last partitioned-tests-device)))))

(context "Unknown Device:"
  (let [device-parser (uap-clj.udf.hive.generic.Device.)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject.
                                     unknown-useragent)])
        result (.evaluate device-parser deferred-args)
        [expected-family expected-brand expected-model]
          ["Other" "" ""]
        [actual-family actual-brand actual-model]
          (map #(.toString %) (into-array Text result))]
    (describe (format "A user agent '%s'" unknown-useragent)
      (it (format "is in the '%s' device family" expected-family)
        (should= expected-family actual-family))
      (it (format "has '%s' as its brand" expected-brand)
        (should= expected-brand actual-brand))
      (it (format "has '%s' as its model" expected-model)
        (should= expected-model actual-model)))))

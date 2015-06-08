(ns uap-clj.udf.hive.generic.device-spec
  (:import [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF$DeferredObject
                                                  GenericUDF$DeferredJavaObject])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.common-spec :refer :all]
            [uap-clj.udf.hive.generic.device :refer [-evaluate]]))

(context "Unknown Device"
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
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as brand '%s'" expected-brand)
        (should= expected-brand actual-brand))
      (it (format "is categorized as model '%s'" expected-model)
        (should= expected-model actual-model)))))

(context "Known Device"
  (let [device-parser (uap-clj.udf.hive.generic.Device.)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject.
                                     known-useragent)])
        result (.evaluate device-parser deferred-args)
        [expected-family expected-brand expected-model]
          ["Spider" "Spider" "Desktop"]
        [actual-family actual-brand actual-model]
          (map #(.toString %) (into-array Text result))]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as brand '%s'" expected-brand)
        (should= expected-brand actual-brand))
      (it (format "is categorized as model '%s'" expected-model)
        (should= expected-model actual-model)))))

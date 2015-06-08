(ns uap-clj.udf.hive.generic.browser-spec
  (:import [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF$DeferredObject
                                                  GenericUDF$DeferredJavaObject])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.common-spec :refer :all]
            [uap-clj.udf.hive.generic.browser :refer [-evaluate]]))

(context "Unknown Browser"
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
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch)))))

(context "Known Browser"
  (let [browser-parser (uap-clj.udf.hive.generic.Browser.)
        deferred-args (into-array GenericUDF$DeferredObject
                                  [(GenericUDF$DeferredJavaObject.
                                     known-useragent)])
        result (.evaluate browser-parser deferred-args)
        [expected-family expected-major expected-minor expected-patch]
          ["Crawler" "" "" ""]
        [actual-family actual-major actual-minor actual-patch]
          (map #(.toString %) (into-array Text result))]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch)))))

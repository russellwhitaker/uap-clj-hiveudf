(ns uap-clj.udf.hive.os-spec
  (:import [org.apache.hadoop.io Text])
  (:require [speclj.core :refer :all]
            [clojure.string :as s]
            [uap-clj.udf.hive.common-spec :refer :all]
            [uap-clj.udf.hive.os :refer [-evaluate]]))

(context "Unknown O/S"
  (let [os-parser (uap-clj.udf.hive.OS.)
        ua (Text. unknown-useragent)
        [expected-family expected-major expected-minor expected-patch expected-patch-minor]
          (s/split "Other\t\t\t\t" #"\t")
        [actual-family actual-major actual-minor actual-patch actual-patch-minor]
          (s/split (str (#'-evaluate os-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch))
      (it (format "is categorized as patch_minor '%s'" expected-patch-minor)
        (should= expected-patch-minor actual-patch-minor)))))

(context "Known O/S"
  (let [os-parser (uap-clj.udf.hive.OS.)
        ua (Text. known-useragent)
        [expected-family expected-major expected-minor expected-patch expected-patch-minor]
          (s/split "Windows XP\t\t\t\t" #"\t")
        [actual-family actual-major actual-minor actual-patch actual-patch-minor]
          (s/split (str (#'-evaluate os-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch))
      (it (format "is categorized as patch_minor '%s'" expected-patch-minor)
        (should= expected-patch-minor actual-patch-minor)))))

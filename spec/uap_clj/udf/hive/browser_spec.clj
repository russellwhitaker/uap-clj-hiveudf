(ns uap-clj.udf.hive.browser-spec
  (:import [org.apache.hadoop.io Text])
  (:require [speclj.core :refer :all]
            [uap-clj.udf.hive.browser :refer [-evaluate]]))

(context "Browser"
  (let [browser-parser (uap-clj.udf.hive.Browser.)
        useragent "Foo 2.3.3"
        expected "Other\t<empty>\t<empty>\t<empty>"]
    (describe (format "A user agent '%s'" useragent)
      (it "is categorized as 'Other'"
        (should= expected
                 (str (#'-evaluate browser-parser
                                   (org.apache.hadoop.io.Text.
                                     useragent))))))))

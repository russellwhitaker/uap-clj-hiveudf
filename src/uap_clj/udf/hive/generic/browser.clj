(ns uap-clj.udf.hive.generic.browser
  (:import [org.apache.hadoop.hive.ql.udf UDFType]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF
                                                  GenericUDF$DeferredObject]
           [java.util ArrayList]
           [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.serde2.objectinspector ObjectInspectorFactory]
           [org.apache.hadoop.hive.ql.exec Description]
           [com.esotericsoftware.kryo DefaultSerializer])
  (:require [uap-clj.core :refer [extract-browser-fields regexes-browser]]
            [uap-clj.udf.hive.generic.common :refer [browser-fieldnames
                                                     check-arguments
                                                     get-struct-field-ois]])
  (:gen-class
   :name ^{org.apache.hadoop.hive.ql.udf.UDFType {:deterministic true}
           org.apache.hadoop.hive.ql.exec.Description
             {:name "browser"
              :value "Takes a useragent & returns struct<family,major,minor,patch>"
              :extended "(type: struct<family:string,major:string,minor:string,patch:string>)"}
           com.esotericsoftware.kryo.DefaultSerializer
             {:value kryo.ext.GenericUDFSerializer}}
         uap-clj.udf.hive.generic.Browser
   :extends org.apache.hadoop.hive.ql.udf.generic.GenericUDF
   :init init
   :state state))

(defn -init []
  [[] (atom {})])

(defn -initialize
  [this object-inspectors]
  (check-arguments this object-inspectors)
  (swap! (.state this)
         assoc :element-oi (aget object-inspectors 0))
  (let [struct-fieldnames browser-fieldnames
        struct-field-object-inspectors (get-struct-field-ois struct-fieldnames)]
    (ObjectInspectorFactory/getStandardStructObjectInspector struct-fieldnames
                                                             struct-field-object-inspectors)))

(defn -evaluate
  [this #^"[Lorg.apache.hadoop.hive.ql.udf.generic.GenericUDF$DeferredObject;" arguments]
  (when arguments
    (let [ua (extract-browser-fields (.toString (.get (aget arguments 0))) regexes-browser)]
      (object-array (vec (map #(Text. (str (get ua (keyword %) nil))) browser-fieldnames))))))

(defn -getDisplayString
  [_ children]
  (format "Looks up useragent browser fields for '%s'" (aget children 0)))

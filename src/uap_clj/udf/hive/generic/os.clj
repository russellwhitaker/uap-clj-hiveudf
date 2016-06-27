(ns uap-clj.udf.hive.generic.os
  (:import [org.apache.hadoop.hive.ql.udf UDFType]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF
                                                  GenericUDF$DeferredObject]
           [java.util ArrayList]
           [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.serde2.objectinspector ObjectInspectorFactory]
           [org.apache.hadoop.hive.ql.exec Description]
           [org.apache.hive.com.esotericsoftware.kryo DefaultSerializer])
  (:require [uap-clj.os :refer [os]]
            [uap-clj.udf.hive.generic.common :refer [os-fieldnames
                                                     check-arguments
                                                     get-struct-field-ois]])
  (:gen-class
   :name ^{org.apache.hadoop.hive.ql.udf.UDFType {:deterministic true}
           org.apache.hadoop.hive.ql.exec.Description
             {:name "os"
              :value "Takes a useragent & returns struct<family,major,minor,patch,patch_minor>"
              :extended "(type: struct<family:string,major:string,minor:string,patch:string,patch_minor:string>)"}
           org.apache.hive.com.esotericsoftware.kryo.DefaultSerializer
             {:value kryo.ext.GenericUDFSerializer}}
         uap-clj.udf.hive.generic.OS
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
  (let [struct-fieldnames os-fieldnames
        struct-field-object-inspectors (get-struct-field-ois struct-fieldnames)]
    (ObjectInspectorFactory/getStandardStructObjectInspector struct-fieldnames
                                                             struct-field-object-inspectors)))

(defn -evaluate
  [this #^"[Lorg.apache.hadoop.hive.ql.udf.generic.GenericUDF$DeferredObject;" arguments]
  (when arguments
    (let [ua (os (.toString (.get (aget arguments 0))))]
      (object-array (vec (map #(Text. (str (get ua (keyword %) nil))) os-fieldnames))))))

(defn -getDisplayString
  [_ children]
  (format "Looks up useragent o/s fields for '%s'" (aget children 0)))

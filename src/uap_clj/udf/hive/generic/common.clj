(ns uap-clj.udf.hive.generic.common
  (:import [java.util ArrayList]
           [org.apache.hadoop.hive.serde Constants]
           [org.apache.hadoop.hive.serde2.objectinspector ObjectInspector$Category]
           [org.apache.hadoop.hive.serde2.objectinspector.primitive PrimitiveObjectInspectorFactory]
           [org.apache.hadoop.hive.ql.exec UDFArgumentTypeException
                                           UDFArgumentLengthException]))

(def browser-fieldnames (ArrayList. ["family" "major" "minor" "patch"]))
(def os-fieldnames (ArrayList. ["family" "major" "minor" "patch" "patch_minor"]))
(def device-fieldnames (ArrayList. ["family" "brand" "model"]))

(defn check-arguments
  [this object-inspectors]
  (when-not (= (alength object-inspectors) 1)
    (throw (UDFArgumentLengthException.
            (format "Function '%s' accepts only 1 argument but was passed '%s' instead."
                    (.getCanonicalName (class this))
                    (alength object-inspectors)))))
  (when-not (= (.getCategory (aget object-inspectors 0))
               ObjectInspector$Category/PRIMITIVE)
    (throw (UDFArgumentTypeException.
            0 (format
                "The argument of function '%s' must be a %s but a %s was passed in instead."
                (.getCanonicalName (class this))
                Constants/STRING_TYPE_NAME
                (.getTypeName (aget object-inspectors 0)))))))

(defn get-struct-field-ois
  ^java.util.ArrayList [fieldnames]
  (ArrayList.
    (vec (take (count fieldnames)
               (repeat PrimitiveObjectInspectorFactory/writableStringObjectInspector)))))

(ns clj-exif.core
  "General structure is:
  -> metadata
  ----> directory
  -------> tag (metadata, value)"
  (:require [clojure.java.io :as jio]
            [clojure.pprint :refer :all]
            )
  (:import [org.apache.commons.imaging Imaging]
           [org.apache.commons.imaging.formats.tiff.write TiffOutputSet]
           [org.apache.commons.imaging.formats.tiff.constants AllTagConstants]
           [org.apache.commons.imaging.formats.tiff.constants ExifTagConstants]
           [org.apache.commons.imaging.formats.tiff.constants TiffTagConstants]
           [org.apache.commons.imaging.formats.tiff.constants TiffDirectoryConstants]
           [org.apache.commons.imaging.common RationalNumber]
           [java.io FileOutputStream]
           [org.apache.commons.imaging.formats.jpeg.exif ExifRewriter]
           [java.io BufferedOutputStream]
           ))


(defn get-metadata
  "Get metadata for a filepath"
  [^java.io.File file]
  (-> file
      (Imaging/getMetadata)))

(defn get-directory
  "Get directory of directory-type from metadata.
  See: TiffDirectoryConstants"
  [metadata directory-type]
  (-> metadata .getExif (.findDirectory directory-type)))

(defn get-directories
 "Read only directories present in metadata."
  [metadata]
  (let [directories (-> metadata .getExif .getDirectories)]
    (map #(get-directory metadata (.type %)) directories)))

;; either get a read directory or a writeable output set
(defn get-output-set
  "Writeable metadata directories.
  Returns: TiffOutputSet"
  [metadata]
  (-> metadata .getExif .getOutputSet))

(defn get-output-directory
  [output-set directory-type]
  (.findDirectory output-set directory-type))

(defn get-fields
  "Give a directory, get all field entries."
  [directory]
  (into [] (.getDirectoryEntries directory)))

(defn fields->map
  "Convert field entries to a clojure map."
  [fields]
  (into {} (map (fn [field]
                  [(.getTagName field) (.getValue field)]) fields)))

(defn crude-exif-map
  "Generate a clojure map of directories/data from metadata."
  [metadata]
  (->> metadata
       get-directories
       (map (fn [directory]
                [(.description directory)
                 (-> directory get-fields fields->map)]))
       (into {})))

(defn copy-file-with-new-metadata
  [from-file to-file output-set]
  (let [exif-rewriter (ExifRewriter.)
        output-buffer (-> to-file
                          (FileOutputStream.)
                          (BufferedOutputStream.))]
    (.updateExifMetadataLossless exif-rewriter
                                 from-file
                                 output-buffer
                                 output-set)))

(defn update-field
  [output-dir tag-info value]
  (let [new-value (into-array (class value) [value])]
  (doto output-dir
    (.removeField tag-info)
    (.add tag-info new-value))))

(defn get-tag-info
  [output-dir tag]
  (-> output-dir
      (.findField tag)
      .tagInfo))

(defn get-value
  [metadata tag-info]
  (.findEXIFValueWithExactMatch metadata tag-info))
 
(comment
  ;; read examples
  (->> "/home/oliver/IMG_20140607_163042357.jpg"
       get-metadata
       get-directories
       (map #(-> % get-fields fields->map)))
  
(-> "/home/oliver/IMG_20140607_163042357.jpg"
    get-metadata
    (get-directory TiffDirectoryConstants/DIRECTORY_TYPE_ROOT)
    get-fields
    fields->map
    )

(-> "/home/oliver/IMG_20140607_163042357.jpg"
      get-metadata
      get-output-set)
  
  (-> "/home/oliver/output2.jpg"
      get-metadata
      crude-exif-map
      pprint
      )

(-> "/home/oliver/IMG_20140607_163042357.jpg"
      get-metadata
      crude-exif-map
      pprint
      )

  (-> "/home/oliver/IMG_20140607_163042357.jpg"
      get-metadata
      (get-directory TiffDirectoryConstants/DIRECTORY_TYPE_ROOT)
      get-fields
      fields->map)

(let [f "/home/oliver/IMG_20140607_163042357.jpg"
      input-file (java.io.File. f)
      output-file (java.io.File. "/home/oliver/output3.jpg")
      md (get-metadata input-file)
      output-set (get-output-set md)
      output-dir (get-output-directory
                        output-set
                        TiffDirectoryConstants/DIRECTORY_TYPE_ROOT)
      tag-to-modify TiffTagConstants/TIFF_TAG_MAKE
      tag-info (get-tag-info output-dir tag-to-modify)
      current-field-value (get-value md tag-info)
      new-field-value "OLICORP2"]

  (println (.getSeperateValue (.findField output-dir tag-info)))

  #_(update-field output-dir tag-info new-field-value)
  #_(copy-file-with-new-metadata input-file output-file output-set))


  (let [f (java.io.File. "/home/oliver/IMG_20140607_163042357.jpg")
        md (Imaging/getMetadata f)
        output-set (.getOutputSet (.getExif md))
        exif-dir (get-directory output-set TiffDirectoryConstants/DIRECTORY_TYPE_ROOT)
        key-to-set TiffTagConstants/TIFF_TAG_MAKE
        field-tag-info (.tagInfo (.findField exif-dir key-to-set))
        current-field-value (.findEXIFValueWithExactMatch md field-tag-info)]
    (println (.getStringValue current-field-value)))

  )



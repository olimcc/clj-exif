(ns clj-exif.examples
  "Examples usage of the exif utilities."
  (:require [clojure.java.io :as jio]
            [clojure.pprint :as pprint]
            [clj-exif.core :as exif])
  (:import
    [java.io File]
    [org.apache.commons.imaging.formats.tiff.constants
     TiffDirectoryConstants
     TiffTagConstants
     GpsTagConstants]
    [org.apache.commons.imaging.common RationalNumber]))

;; read data
(let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
      metadata (exif/get-metadata input-file)]
  (println (pprint/pprint (exif/read metadata))))

(let [input-file (File. "/tmp/output.jpg")
      metadata (exif/get-metadata input-file)]
  (println (pprint/pprint (exif/read metadata))))

;; update the make of the device
(let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
      output-file (File. "/tmp/output.jpg")
      metadata (exif/get-metadata input-file)
      ;; output-set is a writeable copy of the data retrieved from input-file.
      output-set (exif/get-output-set metadata)]
  (exif/update-value output-set
                     TiffDirectoryConstants/DIRECTORY_TYPE_ROOT
                     TiffTagConstants/TIFF_TAG_MAKE
                     ["this is a new value"])
  (exif/copy-file-with-new-metadata input-file output-file output-set))

;; write a new property, 'Artist'
(let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
      output-file (File. "/tmp/output.jpg")
      metadata (exif/get-metadata input-file)
      ;; output-set is a writeable copy of the data retrieved from input-file.
      output-set (exif/get-output-set metadata)]
  (exif/update-value output-set
                     TiffDirectoryConstants/DIRECTORY_TYPE_ROOT
                     TiffTagConstants/TIFF_TAG_ARTIST
                     ["Fancy Artist"])
  (exif/copy-file-with-new-metadata input-file output-file output-set))

;; update the longitude co-ordinate
(let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
      output-file (File. "/tmp/output.jpg")
      metadata (exif/get-metadata input-file)
      ;; output-set is a writeable copy of the data retrieved from input-file.
      output-set (exif/get-output-set metadata)]
  (exif/update-value output-set
                     TiffDirectoryConstants/DIRECTORY_TYPE_GPS
                     GpsTagConstants/GPS_TAG_GPS_LONGITUDE
                     [(RationalNumber. 11 1)
                      (RationalNumber. 11 1)
                      (RationalNumber. 32 100)])
  (exif/copy-file-with-new-metadata input-file output-file output-set))



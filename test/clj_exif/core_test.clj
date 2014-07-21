(ns clj-exif.core-test
  (:require [clj-exif.core :as exif]
            [clojure.test :refer :all]
            [clojure.java.io :as jio])
  (:import [org.apache.commons.imaging.common RationalNumber]
           [java.io File]
           [org.apache.commons.imaging.formats.tiff.constants
            TiffDirectoryConstants
            TiffTagConstants]))

(deftest read-exif-metadata
  (testing "read exif"
    (let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
          metadata (exif/get-metadata input-file)
          data (exif/read metadata)]
      (is (map? data))
      (is (= (get-in data ["Root" "Software"]) "Google"))
      (is (= (class (get-in data ["Root" "XResolution"])) RationalNumber))
      (let [version (get-in data ["Exif" "ExifVersion"])]
        (is (= (count version) 4))
        (is (= (first version) 48)))
      (let [lng (get-in data ["Gps" "GPSLongitude"])]
        (is (= (long (first lng)) 121))))))

(deftest write-exif-metadata
  (testing "write exif"
    (let [output-file (File/createTempFile "/tmp/output" ".jpg")]
    (try
      (let [input-file (jio/as-file (jio/resource "pic.geo.jpg"))
            metadata (exif/get-metadata input-file)
            output-set (exif/get-output-set metadata)
            rand-str (str (rand-int 10000))]
        (exif/update-value output-set
                           TiffDirectoryConstants/DIRECTORY_TYPE_ROOT
                           TiffTagConstants/TIFF_TAG_MAKE
                           [rand-str])
        (exif/copy-file-with-new-metadata input-file output-file output-set)
        (is  (= (-> output-file exif/get-metadata exif/read (get-in ["Root" "Make"]))
                rand-str)))
      (finally (.delete output-file))))))


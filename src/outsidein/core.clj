(ns outsidein.core
  (:use resrc.core
        [clojure.contrib.json :only [read-json]]
        [resrc-client.core :only [resource subresource]])
  (:require [clj-http.client :as http]
            [clojure.contrib.string :as s])
  (:import org.apache.commons.codec.digest.DigestUtils
           java.net.URLEncoder))

(def *api-root* "http://hyperlocal-api.outside.in/v1.1/")

(defn sig
  [key secret]
  (DigestUtils/md5Hex (str key secret (long (/ (System/currentTimeMillis) 1000)))))

(defn wrap-oi-creds
  [client key secret]
  (fn [{query-params :query-params :as req}]
    (client (assoc req :query-params
                   (assoc query-params
                     :dev_key key
                     :sig (sig key secret))))))

(defn oi-http-client
     [key secret]
     (-> http/request
         (wrap-oi-creds key secret)))

(defn url-format
  [path & args]
  (apply format path (map #(URLEncoder/encode (str %)) args)))

(defn oi-resource
  ([client] (resource *api-root* client)))

(defn oi-subresource
  ([root path & args]
     (subresource root (apply url-format path args))))

(defn stories-resource
  ([root state city nabe]
     (oi-subresource root "states/%s/cities/%s/nabes/%s/stories" state city nabe))
  ([root state city]
     (oi-subresource root "states/%s/cities/%s/stories" state city))
  ([root state]
     (oi-subresource root "states/%s/stories" state)))

(defn stories-by-uuid-resource
  [root & uuids]
  (oi-subresource root "locations/%s/stories" (s/join "," uuids)))

(defn stories-by-zipcode-resource
  [root zipcode]
  (oi-subresource root "zipcodes/%s/stories" zipcode))

(defn nearby-resource
  [root lat lng]
  (oi-subresource root "nearby/%s,%s/stories" lat lng))

(defn locations-resource
  ([root name]
     (oi-subresource root "locations/named/%s" name)))

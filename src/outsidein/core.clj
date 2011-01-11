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

(comment

  ;; examples:

  (def root (oi-resource (oi-http-client "key" "secret")))

  (map
   :summary
   (:stories
    (read-json
     (:body (GET (stories-resource root "NY"))))))

  (use '[resrc-client.core :only [?q]])
  (count (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :limit 20))))))

  (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :page 2)))))
  (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :radius 900)))))
  (:stories (read-json (:body (GET (stories-by-zipcode-resource root 94117) (?q :places-only true)))))

  ;; story finder
  (GET (stories-by-uuid-resource
        root
        (:uuid (first (:locations (read-json (:body (GET (locations-resource
                                                          root
                                                          "ithaca,ny")))))))))

  ;; news about the first 5 springfields returned
  (GET (apply stories-by-uuid-resource
              root
              (take 5 (map :uuid (:locations (read-json (:body (GET (locations-resource
                                                                     root
                                                                     "springfield")))))))))

)

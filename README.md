NNTP2P: A caching NNTP Proxy
============================

Introduction
------------
This is a very quick and dirty implementation of a peering, caching NNTP proxy.

### Client Support
The first release aims to target simple clients that only retrieve individual articles by message id, eg: NZB clients.

### Caching strategy
Naive by design, the proxy will attempt to serve requests from peered caches first.
Should articles be unavailable it will fulfil the request from configured upstream servers.

Additionally, it allows you to designate upstream servers as 'backup' servers that will not be queried unless both a cache and primary attempt have failed. 


Feature Roadmap
---------------
This is a work in progress, the following items are on the immediate horizon

* Working v1.0
* Replace pools with queues/broadcast
* Add SSL support for inbound clients
* Add authentication for clients
  - Move cache flag into user profile
  - Usage tracking
* Asynchronous cache requests across cluster
* Configurable cache providers / tiering
* Detect missing articles
* Preseeding NZB interface
* Cache loop detection


License
-------
Copyright 2014

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
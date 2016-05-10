NNTP2P: A caching NNTP Proxy
============================

[![Join the chat at https://gitter.im/dowlingw/nntp2p](https://badges.gitter.im/dowlingw/nntp2p.svg)](https://gitter.im/dowlingw/nntp2p?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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

* Version 1.0
    - Make cache writes happen asynchronously in another thread
    - Enforce cache disk limit
    - Add SSL support for inbound clients
    - User accounting
* Address pool timeouts/etc
* Modify `resolveProvider()` to return an execution plan (ordered list) instead of single source
* Detect missing articles
* Article preseeding interface
* Cache loop detection?
* Automatic cache discovery


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
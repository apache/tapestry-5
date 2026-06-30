<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Agent Guide for Apache Tapestry

This file is read by automated agents (security scanners, code analyzers,
AI assistants) operating on this repository.

## Security

Security model: [SECURITY.md](./SECURITY.md) -> [THREAT_MODEL.md](./THREAT_MODEL.md)

Agents that scan this repository should consult `SECURITY.md` and the linked
`THREAT_MODEL.md` before reporting issues. Tapestry is a component-based web
framework: the untrusted web client is the adversary, while application
pages/components/handlers and configuration are trusted. The single most
load-bearing mechanism is the HMAC-protected serialized client state
(deserialization is gated by `tapestry.hmac-passphrase`); the model also covers
default output escaping, asset access control, client whitelisting, and
secure-link enforcement.

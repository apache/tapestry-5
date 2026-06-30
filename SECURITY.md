# Security Policy

## Reporting a Vulnerability

Apache Tapestry follows the [Apache Software Foundation security process](https://www.apache.org/security/).
Please report suspected vulnerabilities **privately** to `security@apache.org` (the Tapestry PMC is reachable
at `private@tapestry.apache.org`). Do **not** open public GitHub issues or pull requests for security reports.

## Threat Model

What Tapestry treats as in/out of scope, the security properties it provides and disclaims (HMAC-gated
serialized-state deserialization, default output escaping, asset access control, client whitelisting,
secure-link enforcement), the adversary model (the untrusted web client vs. the trusted application
developer/operator), and how findings are triaged are documented in [THREAT_MODEL.md](./THREAT_MODEL.md).

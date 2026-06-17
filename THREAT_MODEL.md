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

# Threat Model — Apache Tapestry (5)

## §1 Header

- **Project:** Apache Tapestry — a **component-based Java web application framework**. Developers build pages
  and components; Tapestry handles request dispatch, rendering, form/event processing, asset serving, and
  serialization of some server-side state into the client (page activation context, form data) which it
  validates and deserializes on postback *(documented — README; source `tapestry-core`, `tapestry-http`)*.
- **Modelled against:** `apache/tapestry-5` `master`/HEAD (2026-05-31).
- **Status:** **DRAFT — v0, not yet reviewed by the Tapestry PMC.** Produced by the ASF Security team via the
  `threat-model-producer` rubric (<https://gist.github.com/potiuk/da14a826283038ddfe38cc9fe6310573>).
- **Reporting / version-binding / legend** as in the sibling models. **Draft confidence:** ~12 documented /
  0 maintainer / ~46 inferred. Each *(inferred)* routes to §14.

**Framing note:** Tapestry is a *framework*. The **application developer** authors pages, components,
templates, and event handlers — that code is **trusted** (§3). The **untrusted web client** sending requests,
form posts, and activation-context URLs is the adversary (§7). The single most security-load-bearing
mechanism is the **HMAC-protected serialized client state**: Tapestry round-trips serialized objects through
the browser and deserializes them on return, so their integrity rests on a configured HMAC secret.

## §2 Scope and intended use

Intended use *(documented)*: build and serve a Java web application; clients interact over HTTP(S) with
rendered pages, forms, and component events.

Caller roles:

- **Web client (untrusted)** — any browser/agent issuing requests, form posts, activation-context URLs.
- **Application developer** — authors pages/components/templates/handlers and chooses where to use raw output,
  uploads, whitelisting, HTTPS. **Trusted; out of model as adversary (§3).**
- **Operator/deployer** — sets `tapestry.hmac-passphrase`, production mode, and deployment hardening.
  **Trusted; out of model (§3).**

**Component-family table:**

| Family | Entry point | Touches outside process | In model? |
| --- | --- | --- | --- |
| Request dispatch + page activation | URL → page/event, activation context | — | **Yes** |
| Serialized client state + **HMAC** | `t:formdata` / activation serialization, deserialize on postback | **deserialization** | **Yes (critical)** |
| Rendering / template output | component render, output escaping | — | **Yes (XSS)** |
| Forms + file upload | form submit, multipart upload | fs (temp) | **Yes** |
| Asset serving | classpath/context asset URLs | filesystem/classpath | **Yes (traversal)** |
| Access whitelisting | `@WhitelistAccessOnly`, `ClientWhitelist`/`LocalhostOnly` | client address | **Yes** |
| Transport/link security | `RequestSecurityManager`, `LinkSecurity` (HTTP↔HTTPS) | network | **Yes** |
| Tests / sample apps / docs | **all** `src/test/**` across modules (incl. `tapestry-core/src/test`), samples, docs — none of it deploys | — | No → §3 |

## §3 Out of scope (explicit non-goals)

- **The application developer and operator as adversaries**, and the application's own page/component/handler
  code — that is trusted authored code, not an adversary surface (§7) *(inferred)*.
- **Misconfiguration** (no HMAC passphrase set, raw output of untrusted data, exposing a whitelisted page) —
  Tapestry provides the controls; using them is the developer/operator's job (§9/§10/§11).
- **The application's business-logic authorization** beyond the framework's whitelist/secure-link mechanisms.
- **All internal test code (`src/test/**` in every module, e.g.
  `tapestry-core/src/test`), sample apps (`app1`), and documentation** — none
  of it is deployed, so it is not an adversary surface *(maintainer —
  thiagohp)*.
- **The JVM serialization/JCE internals** except as Tapestry selects and uses them.

## §4 Trust boundaries and data flow

The boundary is the **HTTP request**: parameters, form data, activation context, and serialized client state
are untrusted until validated *(inferred)*.

Trust transitions:

1. **URL → page activation/event:** path/query map to a page, an event, and an activation context. Untrusted
   context values reach handler parameters *(inferred)*.
2. **Postback → deserialize serialized state (the critical one):** Tapestry deserializes the serialized
   object stream it previously sent to the client. It is accepted **only if its HMAC verifies** against the
   configured passphrase — this is what stops an attacker from submitting an arbitrary serialized object and
   achieving deserialization RCE *(inferred — `RequestSecurityManager`, HMAC mechanism; load-bearing, §14)*.
3. **Render → output:** component output is HTML-escaped by default; raw output is an explicit developer
   opt-in *(inferred)*.
4. **Asset URL → file:** asset requests resolve to classpath/context resources; path canonicalization must
   prevent traversal/arbitrary read *(inferred)*.
5. **Whitelist gate:** `@WhitelistAccessOnly` pages/services are served only to whitelisted clients (e.g.
   localhost) *(documented — `ClientWhitelist`, `LocalhostOnly`)*.

**Reachability precondition:** in-model if reachable from an untrusted request before the framework's
HMAC/whitelist/escaping controls; a finding requiring the operator to have left the HMAC passphrase unset or
the developer to have emitted raw untrusted output is `OUT-OF-MODEL: non-default-build` / misconfig (§5a/§3).

## §5 Assumptions about the environment

- A servlet container hosting the Tapestry app; a JVM.
- `tapestry.hmac-passphrase` is configured to a strong secret by the operator *(inferred — wave-1)*.
- Production mode disables developer conveniences (detailed exception pages, component reload) *(inferred)*.
- TLS is provided by the container; Tapestry's secure-link/`RequestSecurityManager` enforces HTTPS for pages
  marked secure *(inferred)*.
- **What Tapestry does to its host (*(inferred)* — wave-2):** reads classpath/context assets; writes temp
  files for uploads; deserializes HMAC-validated client state; not assumed to open arbitrary sockets or run
  host commands.

## §5a Build-time and configuration variants

| Knob | Effect | Ruling needed |
| --- | --- | --- |
| `tapestry.hmac-passphrase` | Integrity of serialized client state ⇒ deserialization-RCE protection | **Open (wave-1):** is it required / fail-closed when unset, or does an unset value void §8.1? |
| Production mode vs. dev | Exposure of stack traces, component source, reload | **Open (wave-1):** prod defaults |
| Output escaping default | XSS posture (raw output opt-in) | **Open (wave-1):** escaped by default? |
| Asset path / `tapestry.asset-path-prefix` + protection | Traversal/arbitrary-read posture | Confirm |
| `@WhitelistAccessOnly` analyzer (default LocalhostOnly) | Who may reach whitelisted pages | Confirm default |
| Secure-link / `RequestSecurityManager` | HTTPS enforcement for secure pages | Developer choice |

## §6 Assumptions about inputs

| Entry point | Parameter | Attacker-controllable? | Caller/operator must enforce |
| --- | --- | --- | --- |
| page/event request | activation context, event context, query/form params | **yes** | type coercion; handler validation |
| postback | `t:formdata` / serialized client state | **yes** | **HMAC verification before deserialization** |
| asset request | asset path/URL | **yes** | path canonicalization; no traversal |
| file upload | filename, body, content-type | **yes** | size/type limits; safe temp handling |
| whitelisted page/service | request origin/address | **yes** | whitelist analyzer (default localhost) |
| `tapestry.*` config (hmac passphrase, mode) | all | **no — operator-trusted** | never sourced from a request |

## §7 Adversary model

- **Primary adversary:** an untrusted web client. Capabilities: submit crafted activation/event contexts,
  tampered or replayed serialized client state, malicious asset paths, oversized/typed uploads, and content
  intended to reflect as XSS.
- **Goals:** deserialization RCE via forged client state (defeated only by the HMAC); arbitrary file read via
  asset traversal; reach a whitelisted/admin page; stored/reflected XSS; DoS via large uploads/contexts.
- **Out of model:** the application developer and operator; anyone holding the HMAC passphrase or filesystem
  access.

## §8 Security properties the project provides

*(Conditional on configuration; *(inferred)* pending §14.)*

1. **Serialized-state integrity (deserialization-RCE protection).** Serialized client state is deserialized
   only after its **HMAC verifies** against the configured passphrase, so an attacker cannot submit an
   arbitrary serialized object *(inferred — load-bearing; the post-CVE-2021-27850 protection)*. *Symptom:*
   accepted forged serialized object ⇒ RCE. *Severity:* critical.
2. **Output escaping by default.** Rendered component output is HTML-escaped unless the developer explicitly
   emits raw output *(inferred)*. *Symptom:* reflected/stored XSS from framework-rendered values. *Severity:*
   high–critical.
3. **Asset access control.** Asset URLs resolve only to intended classpath/context resources; traversal /
   arbitrary file read is prevented *(inferred)*. *Symptom:* read of files outside the asset roots. *Severity:*
   critical.
4. **Whitelist enforcement.** `@WhitelistAccessOnly` resources are served only to whitelisted clients (default
   localhost) *(documented — `ClientWhitelist`/`LocalhostOnly`)*. *Symptom:* a whitelisted page reachable by a
   non-whitelisted client. *Severity:* high.
5. **Secure-link / HTTPS enforcement.** Pages marked secure are served/linked over HTTPS via
   `RequestSecurityManager`/`LinkSecurity` *(documented — classes present)*. *Symptom:* secure page served over
   plain HTTP. *Severity:* medium–high.

## §9 Security properties the project does NOT provide

- **No serialized-state protection without a configured HMAC passphrase** — if unset/weak, §8.1 is void and the
  deserialization surface reopens *(inferred — wave-1)*.
- **No XSS protection for developer-emitted raw output** — `OutputRaw`/raw markup of untrusted data is the
  developer's responsibility (§10).
- **No defence against the application developer or operator** (§3).
- **No application-level authorization** beyond the whitelist/secure-link mechanisms; page-level access control
  for normal pages is the app's job.

**False friends:**

- *The serialized client state looks like opaque framework plumbing but is an attacker-reachable
  deserialization channel* — its safety is entirely the HMAC; protect the passphrase like a key.
- *Whitelist "access only" looks like authentication but is an address/origin filter* (default localhost), not
  user authn.
- *Default escaping protects framework-rendered values, not raw output a component deliberately emits.*

**Well-known attack classes to keep in view:** Java **deserialization** (gated by the HMAC); **XSS** via raw
output; **path traversal** via asset URLs; **CSRF** on form/event posts; **open redirect** via link/redirect
parameters; upload-based DoS / content-type confusion.

## §10 Downstream (developer/operator) responsibilities

- **Set a strong `tapestry.hmac-passphrase`** and protect it; treat it as a cryptographic key.
- Run in **production mode**; never expose dev exception pages / component sources publicly.
- Never emit **raw (unescaped) output** of untrusted data; rely on default escaping otherwise.
- Use `@WhitelistAccessOnly` for admin/diagnostic pages and confirm the whitelist analyzer fits the
  deployment (default localhost).
- Mark sensitive pages secure (HTTPS) and run behind TLS; set upload size/type limits.

## §11 Known misuse patterns

- Deploying without configuring `tapestry.hmac-passphrase` (reopens the deserialization surface).
- Emitting untrusted data as raw markup (XSS).
- Running in development mode in production (information disclosure).
- Exposing diagnostic/whitelisted pages to the public.
- Trusting activation/event context values without validation in handlers.

## §11a Known non-findings (recurring false positives)

*(v0 seed — the PMC will own the authoritative list — §14.)*

- **"Java deserialization in Tapestry"** reports that ignore the **HMAC gate** — `KNOWN-NON-FINDING` when the
  HMAC verification is in place (the post-CVE-2021-27850 design); only an HMAC bypass or unset passphrase is `VALID`.
- **XSS attributed to a developer's raw output** — developer responsibility (§9/§10), not a framework default.
- **Findings in any `src/test/**` (internal test code, e.g. `tapestry-core/src/test`) / samples / docs** — out of scope (§3).
- **Whitelisted page reachable from localhost** — by design (§8.4).
- **Dev-mode information disclosure** against a dev configuration — operator posture (§5a/§11).

## §12 Conditions that would change this model

- A change to the HMAC/serialized-state protection or default passphrase handling.
- A change to default output escaping or asset path protection.
- A new client-reachable serialization/deserialization path.
- A change to the whitelist analyzer default or secure-link defaults.
- Any report not cleanly routable to a §13 disposition.

## §13 Triage dispositions

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | Violates a claimed property via an in-scope adversary/input in a default/secure config. | §8, §6, §7 |
| `VALID-HARDENING` | No §8 property broken, but a §11 misuse warrants a safer default/guard. | §11 |
| `OUT-OF-MODEL: trusted-input` | Requires control of app code / config / HMAC passphrase. | §6, §3 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires developer/operator/key capability. | §7, §3 |
| `OUT-OF-MODEL: unsupported-component` | Lands in tests, sample apps, docs. | §3 |
| `OUT-OF-MODEL: non-default-build` | Only when the HMAC passphrase was unset or raw output / dev mode used. | §5a |
| `BY-DESIGN: property-disclaimed` | Concerns a §9-disclaimed property (raw output; whitelist ≠ authn). | §9 |
| `KNOWN-NON-FINDING` | Matches a §11a entry. | §11a |
| `MODEL-GAP` | Routes to none of the above → revise the model. | §12 |

## §14 Open questions for the maintainers

**Wave 1 — the deserialization gate + defaults (§5a/§8):**
1. Is a configured **`tapestry.hmac-passphrase`** effectively **required** (fail-closed / hard warning) so the
   serialized-state deserialization (§8.1) is always HMAC-gated in production? What happens if it is unset?
   *Proposed:* required; unset = startup failure/loud warning, not silent insecure default.
2. Is framework-rendered output **HTML-escaped by default**, with raw output an explicit opt-in? *Proposed:* yes.
3. Are **assets** protected against path traversal / arbitrary classpath read by default? *Proposed:* yes.

**Wave 2 — whitelist, secure-link, CSRF (§8/§9):**
4. Confirm the default **whitelist analyzer** (localhost-only) for `@WhitelistAccessOnly`, and how
   `RequestSecurityManager` decides "secure". *Proposed:* default localhost; secure pages per annotation/config.
5. Does Tapestry provide **CSRF** protection for form/event posts, or is that the application's responsibility?
   *Proposed:* application responsibility unless a built-in token exists — confirm.

**Wave 3 — §11a (§11a):**
6. The component categorization you shared earlier — please confirm the §2 family table and the §11a
   non-findings list (especially the "deserialization is HMAC-gated" entry). *Proposed:* per §2/§11a above.
7. What do scanners most often (re)report that the PMC considers a **non-finding**? (Seeds §11a.)

**Meta:**
8. Confirm this model lives as root `THREAT_MODEL.md` referenced from a new `SECURITY.md`. *Proposed:* yes.

## §15 Machine-readable companion

Deferred for v0; a `threat-model.yaml` can later encode the §6 trust table, §2/§3 scoping, §8 rows, §9 false
friends, §11a non-findings, and §13 dispositions.

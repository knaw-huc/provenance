# Provenance

A service for maintaining provenance.

## API

_We assume the web interface is running on localhost on port 8080_

**URL**: `/`\
**Method**: `POST`\
**Parameters**: `who`, `where`, `when`, `how`, `why`, `source`, `source_rel`,  `target`, `target_rel`

Create a new provenance record. \
The parameter `source` is required. Multiple `source` and `target` are allowed, with each having a
corresponding `source_rel` and `target_rel`. \
The parameters `who`, `where`, `when` and `how` should be valid URIs.

**Example (curl)**:

```bash
curl \
  -X POST \
  -d 'who=orcid:123' \
  -d 'source=md5:7815696ecbf1c96e6894b779456d330e' \
  -d 'source_rel=primary' \
  http://localhost:8080/
```

---

**URL**: `/{id}`\
**Method**: `POST`\
**Parameters**: `who`, `where`, `when`, `how`, `why`, `source`, `source_rel`,  `target`, `target_rel`

Updates an existing provenance record. \
Multiple `source` and `target` are allowed, with each having a corresponding `source_rel` and `target_rel`. \
The parameters `who`, `where`, `when` and `how` should be valid URIs.

**Example (curl)**:

```bash
curl \
  -X PUT \
  -d 'who=orcid:123' \
  -d 'source=md5:7815696ecbf1c96e6894b779456d330e' \
  -d 'source_rel=primary' \
  http://localhost:8080/1
```

# Provenance

A service for maintaining provenance.

## API

_We assume the web interface is running on localhost on port 8080_

**URL**: `/prov`\
**Method**: `POST`\
**Parameters**: `who`, `where`, `when`, `how` (or more specifically `how_software`, `how_init`, `how_delta`), `why` (or
more specifically `why_motivation`, `why_provenance_schema`), `source`, `source_rel`,  `target`, `target_rel`

Create a new provenance record. \
The parameter `source` is required. Multiple `source` and `target` are allowed, with each having a
corresponding `source_rel` and `target_rel`. \
The parameters `who`, `where` and `how_software` should be valid URIs. \
The parameter `how_delta` should be a valid delta using
the [Unified format](https://www.gnu.org/software/diffutils/manual/html_node/Detailed-Unified.html#Detailed-Unified).

**Example (curl)**:

```bash
curl \
  -X POST \
  -H "Authorization: Basic: 7c7f89e7-5ce4-4b79-b475-538d5f9ddad0" \
  -d "who=orcid:123" \
  -d "source=md5:7815696ecbf1c96e6894b779456d330e" \
  -d "source_rel=primary" \
  "http://localhost:8080/prov" 
```

**Example (Python)**:

```python3
import requests

response = requests.post('http://localhost:8080/prov', data={
    'who': 'orcid:12345',
    'where': 'http://somelocation.uri',
    'when': '2022-02-02T02:00:00Z',
    'how_software': 'https://github.com/knaw-huc/provenance/commit/b725d0a592961985f0510afed1bc98d118acb32f',
    'how_init': '-i my-data.trig -o my-output.csv',
    'why': 'Motivation',
    'source': ['md5:7815696ecbf1c96e6894b779456d330e', 'file:my-data.trig'],
    'source_rel': ['primary', 'primary'],
    'target': ['file:my-output.csv'],
    'target_rel': ['primary'],
}, headers={'Authorization': 'Basic: 7c7f89e7-5ce4-4b79-b475-538d5f9ddad0'})

print(response.headers['Location'][1:])
```

---

**URL**: `/prov/{id}`\
**Method**: `PUT`\
**Parameters**: `who`, `where`, `when`, `how` (or more specifically `how_software`, `how_init`, `how_delta`), `why` (or
more specifically `why_motivation`, `why_provenance_schema`), `source`, `source_rel`,  `target`, `target_rel`

Updates an existing provenance record. \
Multiple `source` and `target` are allowed, with each having a corresponding `source_rel` and `target_rel`. \
The parameters `who`, `where`, `when` and `how_software` should be valid URIs. \
The parameter `how_delta` should be a valid delta using
the [Unified format](https://www.gnu.org/software/diffutils/manual/html_node/Detailed-Unified.html#Detailed-Unified).

**Example (curl)**:

```bash
curl \
  -X PUT \
  -H "Authorization: Basic: 7c7f89e7-5ce4-4b79-b475-538d5f9ddad0" \
  -d "who=orcid:123" \
  -d "source=md5:7815696ecbf1c96e6894b779456d330e" \
  -d "source_rel=primary" \
  "http://localhost:8080/prov/1" 
```

**Example (Python)**:

```python3
import requests

requests.put('http://localhost:8080/prov/1', data={
    'who': 'orcid:12345',
    'where': 'http://somelocation.uri',
    'when': '2022-02-02T02:00:00Z',
    'how': 'https://github.com/knaw-huc/provenance/commit/b725d0a592961985f0510afed1bc98d118acb32f',
    'why': 'Motivation',
    'source': ['md5:7815696ecbf1c96e6894b779456d330e', 'file:my-data.trig'],
    'source_rel': ['primary', 'primary'],
    'target': ['file:my-output.csv'],
    'target_rel': ['primary'],
}, headers={'Authorization': 'Basic: 7c7f89e7-5ce4-4b79-b475-538d5f9ddad0'})
```

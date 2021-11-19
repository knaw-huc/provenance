'use strict';

window.addEventListener('hashchange', withResource);
withResource();

async function withResource() {
    const resource = window.location.hash.substring(1);
    const trail = await (await fetch(`/trail?resource=${encodeURIComponent(resource)}`)).json();

    createTree(resource, trail);
    writeMetadata(resource, trail);
}

function createTree(resource, trail) {
    const rect = document.querySelector('.container').getBoundingClientRect();
    const width = Math.floor(rect.width);
    const height = 400;

    const svg = d3.select('#tree');
    svg.attr('width', width)
        .attr('height', height)
        .selectAll('*')
        .remove();

    const leftHierarchy = d3.hierarchy({resource: trail.resource, relations: trail.sources}, d => d.relations);
    const rightHierarchy = d3.hierarchy({resource: trail.resource, relations: trail.targets}, d => d.relations);

    const totalHeight = leftHierarchy.height + rightHierarchy.height;
    const leftWidth = ((width - 20) / totalHeight) * leftHierarchy.height;
    const rightWidth = ((width - 20) / totalHeight) * rightHierarchy.height;

    const leftTree = d3.tree().size([height - 20, -leftWidth]);
    const rightTree = d3.tree().size([height - 20, rightWidth]);

    const leftNodes = leftTree(leftHierarchy);
    const rightNodes = rightTree(rightHierarchy);

    const main = svg.append('g').attr('transform', 'translate(10, 10)');
    const leftMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);
    const rightMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);

    for (const [isLeft, nodes, main] of [[true, leftNodes, leftMain], [false, rightNodes, rightMain]]) {
        nodes.x = height / 2;

        const link = main.append('g')
            .attr('fill', 'transparent')
            .attr('stroke', '#ccc')
            .attr('stroke-width', 2)
            .selectAll('path')
            .data(nodes.descendants().slice(1))
            .join('path')
            .style('cursor', 'pointer')
            .attr('data-prov-id', d => d.data.provenanceId)
            .attr('d', d => `M${d.y},${d.x}C${(d.y + d.parent.y) / 2},${d.x} ${(d.y + d.parent.y) / 2},${d.parent.x} ${d.parent.y},${d.parent.x}`)
            .on('click', (e, d) => {
                document.getElementById('metadata')
                    .dispatchEvent(new CustomEvent('provenance', {detail: d.data.provenanceId}));
                e.preventDefault();
            });

        const node = main.append('g')
            .attr('font-family', 'sans-serif')
            .attr('font-size', '8pt')
            .selectAll('g')
            .data(nodes.descendants())
            .join('g')
            .attr('transform', d => `translate(${d.y},${d.x})`);

        node.append('circle')
            .attr('r', 8)
            .attr('fill', d => d.data.resource === resource ? '#cfdeec' : '#fff')
            .attr('stroke', '#4682b4')
            .attr('stroke-width', 2)
            .filter(d => d.data.resource !== resource)
            .on('mouseover', e => d3.select(e.target).style('cursor', 'pointer'))
            .on('mouseout', e => d3.select(e.target).style('cursor', 'default'))
            .on('click', (e, d) => {
                window.location.hash = d.data.resource;
                e.preventDefault();
            });

        node.append('text')
            .attr('dy', '.40em')
            .attr('x', d => !d.children && !isLeft ? -13 : 13)
            .attr('text-anchor', d => !d.children && !isLeft ? 'end' : 'start')
            .text(d => {
                if (d.data.resource.length <= 50) return d.data.resource;
                return '...' + d.data.resource.substring(d.data.resource.length - 50);
            });
    }
}

function writeMetadata(resource, trail) {
    const root = document.getElementById('metadata');
    while (root.firstChild)
        root.removeChild(root.lastChild);

    const header = document.createElement('h1');
    header.innerText = resource;

    const links = document.createElement('dl');
    root.append(header, links);

    if (trail.sources.length > 0)
        writeData(links, 'Is the result of', trail.sources.map(source => createResourceElem(root, source)));

    if (trail.targets.length > 0)
        writeData(links, 'Is used as input for', trail.targets.map(target => createResourceElem(root, target)));

    const provHeader = document.createElement('h2');
    provHeader.innerText = 'Provenance';
    provHeader.className = 'hidden';

    const prov = document.createElement('dl');
    root.append(provHeader, prov);

    root.addEventListener('provenance', async e => {
        const provenanceId = e.detail;
        if (prov.dataset.id === provenanceId)
            return;

        provHeader.className = '';
        document.querySelectorAll(`[data-prov-id="${prov.dataset.id}"]`)
            .forEach(link => link.setAttribute('stroke', '#ccc'));

        while (prov.firstChild)
            prov.removeChild(prov.lastChild);

        prov.dataset.id = provenanceId;

        document.querySelectorAll(`[data-prov-id="${provenanceId}"]`)
            .forEach(link => link.setAttribute('stroke', '#4682b4'));

        const record = await (await fetch(`/prov/${provenanceId}`)).json();
        if (record.who) writeData(prov, 'Who', record.who);
        if (record.where) writeData(prov, 'Where', record.where);
        if (record.when) writeData(prov, 'When', record.when);
        if (record.how) writeData(prov, 'How', record.how);
        if (record.why) writeData(prov, 'Why', record.why);
    });
}

function createResourceElem(root, data) {
    const resourceLink = document.createElement('a');
    resourceLink.innerText = data.resource;
    resourceLink.setAttribute('href', `#${data.resource}`);

    const provLink = document.createElement('a');
    provLink.innerText = 'Show provenance';
    provLink.className = 'provlink';
    provLink.setAttribute('href', '');
    provLink.addEventListener('click', e => {
        root.dispatchEvent(new CustomEvent('provenance', {detail: data.provenanceId}));
        e.preventDefault();
    });

    const resourceElem = document.createElement('dd');
    resourceElem.append(resourceLink, provLink);

    return resourceElem;
}

function writeData(parent, header, values) {
    const headerElem = document.createElement('dt');
    headerElem.innerText = header;

    if (!Array.isArray(values))
        values = [values];

    const valueElems = values.map(value => {
        if (typeof value === 'string') {
            const valueElem = document.createElement('dd');
            valueElem.innerText = value;
            return valueElem;
        }
        return value;
    });

    parent.append(headerElem, ...valueElems);
}

'use strict';

const color = '#4682b4';
const selectedColor = '#cfdeec';

window.addEventListener('hashchange', withData);
withData();

async function withData() {
    const params = new URLSearchParams(window.location.hash.substring(1));
    const trail = await (await fetch(`/trail?${params.toString()}`)).json();

    createTree(trail);
    writeMetadata(trail);
}

function createTree(trail) {
    const rect = document.querySelector('.container').getBoundingClientRect();
    const minWidth = Math.floor(rect.width);
    const height = 400;

    const leftHierarchy = d3.hierarchy(trail.sourceRoot, d => d.relations);
    const rightHierarchy = d3.hierarchy(trail.targetRoot, d => d.relations);

    const totalHeight = leftHierarchy.height + rightHierarchy.height;
    const width = Math.max(minWidth, totalHeight * 150);

    const svg = d3.select('#tree');
    svg.attr('width', width)
        .attr('height', height)
        .selectAll('*')
        .remove();

    svg.append('defs')
        .append('marker')
        .attr('id', 'arrowhead')
        .attr('markerWidth', 5)
        .attr('markerHeight', 4)
        .attr('refX', 10)
        .attr('refY', 2)
        .attr('orient', 'auto')
        .attr('fill', '#ccc')
        .append('polygon')
        .attr('points', '0 0, 5 2, 0 4');

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
            .attr(isLeft ? 'marker-end' : 'marker-start', 'url(#arrowhead)')
            .selectAll('path')
            .data(nodes.descendants().slice(1))
            .join('path')
            .attr('d', d => `M${d.y},${d.x}C${(d.y + d.parent.y) / 2},${d.x} ${(d.y + d.parent.y) / 2},${d.parent.x} ${d.parent.y},${d.parent.x}`);

        const resourceNode = main.append('g')
            .attr('font-family', 'sans-serif')
            .attr('font-size', '8pt')
            .selectAll('g')
            .data(nodes.descendants())
            .join('g')
            .filter(d => d.data.type === 'resource')
            .attr('transform', d => `translate(${d.y},${d.x})`);

        resourceNode.append('circle')
            .attr('r', 8)
            .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            .attr('stroke', color)
            .attr('stroke-width', 2)
            .filter(d => d.depth !== 0)
            .style('cursor', 'pointer')
            .on('click', (e, d) => {
                window.location.hash = `resource=${encodeURIComponent(d.data.resource)}`;
                e.preventDefault();
            });

        // resourceNode.append('text')
        //     .attr('dy', '.40em')
        //     .attr('x', d => !d.children && !isLeft ? -13 : 13)
        //     .attr('text-anchor', d => !d.children && !isLeft ? 'end' : 'start')
        //     .text(d => {
        //         if (d.data.resource.length <= 50) return d.data.resource;
        //         return '...' + d.data.resource.substring(d.data.resource.length - 50);
        //     });

        const provenanceNode = main.append('g')
            .attr('font-family', 'sans-serif')
            .attr('font-size', '8pt')
            .selectAll('g')
            .data(nodes.descendants())
            .join('g')
            .filter(d => d.data.type === 'provenance')
            .attr('transform', d => `translate(${d.y},${d.x})`);

        provenanceNode.append('rect')
            .attr('width', 14)
            .attr('height', 14)
            .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            .attr('stroke', color)
            .attr('stroke-width', 2)
            .attr('transform', 'translate(0, -10) rotate(45)')
            .filter(d => d.depth !== 0)
            .style('cursor', 'pointer')
            .on('click', (e, d) => {
                window.location.hash = `provenance=${encodeURIComponent(d.data.id)}`;
                e.preventDefault();
            });
    }
}

async function writeMetadata(trail) {
    const root = document.getElementById('metadata');
    while (root.firstChild)
        root.removeChild(root.lastChild);

    const header = document.createElement('h1');
    const links = document.createElement('dl');
    root.append(header, links);

    if (trail.sourceRoot.type === 'resource') {
        header.innerText = trail.sourceRoot.resource;

        if (trail.sourceRoot.relations.length > 0)
            writeData(links, 'Is the result of events with provenance identifier',
                trail.sourceRoot.relations.map(source => createProvenanceElem(root, source)));

        if (trail.targetRoot.relations.length > 0)
            writeData(links, 'Is used as input for events with provenance identifier',
                trail.targetRoot.relations.map(target => createProvenanceElem(root, target)));
    }
    else {
        header.innerText = 'Event with provenance id #' + trail.sourceRoot.id;

        if (trail.sourceRoot.relations.length > 0)
            writeData(links, 'The event used the following resources as input',
                trail.sourceRoot.relations.map(source => createResourceElem(root, source)));

        if (trail.targetRoot.relations.length > 0)
            writeData(links, 'Which resulted in the following resources',
                trail.targetRoot.relations.map(target => createResourceElem(root, target)));

        const provHeader = document.createElement('h2');
        provHeader.innerText = 'Provenance';

        const prov = document.createElement('dl');
        root.append(provHeader, prov);

        provHeader.className = '';
        document.querySelectorAll(`[data-prov-id="${trail.sourceRoot.id}"]`)
            .forEach(link => link.setAttribute('stroke', '#ccc'));

        while (prov.firstChild)
            prov.removeChild(prov.lastChild);

        prov.dataset.id = trail.sourceRoot.id;

        document.querySelectorAll(`[data-prov-id="${trail.sourceRoot.id}"]`)
            .forEach(link => link.setAttribute('stroke', '#4682b4'));

        const record = await (await fetch(`/prov/${trail.sourceRoot.id}`)).json();
        if (record.who) writeData(prov, 'Who', record.who);
        if (record.where) writeData(prov, 'Where', record.where);
        if (record.when) writeData(prov, 'When', record.when);
        if (record.how) writeData(prov, 'How', record.how);
        if (record.why) writeData(prov, 'Why', record.why);
    }
}

function createProvenanceElem(root, data) {
    const resourceLink = document.createElement('a');
    resourceLink.innerText = '#' + data.id;
    resourceLink.setAttribute('href', `#provenance=${data.id}`);

    const resourceElem = document.createElement('dd');
    resourceElem.append(resourceLink);

    return resourceElem;
}

function createResourceElem(root, data) {
    const resourceLink = document.createElement('a');
    resourceLink.innerText = data.resource;
    resourceLink.setAttribute('href', `#resource=${data.resource}`);

    const resourceElem = document.createElement('dd');
    resourceElem.append(resourceLink);

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

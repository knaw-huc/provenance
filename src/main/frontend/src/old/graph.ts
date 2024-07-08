import {select, hierarchy, tree} from 'd3';
import {TrailType, Trail, TrailNode, isResource, Resource, Provenance, isProvenance} from '../interfaces.ts';

const color = '#4682b4';
const selectedColor = '#cfdeec';

export default function createGraph() {
    createLegend();

    window.addEventListener('hashchange', withData);
    withData();
}

function createLegend() {
    const svg = select('.legend');
    svg.attr('width', 130)
        .attr('height', 50)
        .selectAll('*')
        .remove();

    const resourceGroup = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(15, 0)');

    resourceGroup.append('circle')
        .attr('cx', 0)
        .attr('cy', 10)
        .attr('r', 8)
        .attr('fill', '#fff')
        .attr('stroke', color)
        .attr('stroke-width', 2);

    resourceGroup.append('text')
        .attr('x', 20)
        .attr('y', 15)
        .attr('text-anchor', 'start')
        .text('Resource');

    const provGroup = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(15, 25)');

    provGroup.append('rect')
        .attr('x', 0)
        .attr('y', 0)
        .attr('width', 14)
        .attr('height', 14)
        .attr('fill', '#fff')
        .attr('stroke', color)
        .attr('stroke-width', 2)
        .attr('transform', 'rotate(45)');

    provGroup.append('text')
        .attr('x', 20)
        .attr('y', 15)
        .attr('text-anchor', 'start')
        .text('Provenance event');
}

async function withData() {
    let id = window.location.hash.substring(1);
    let isResource = isNaN(parseInt(id));
    let atTime = null;
    if (isResource && id.indexOf(';@') > 0)
        [id, atTime] = id.split(';@');

    const params = new URLSearchParams();
    params.append(isResource ? 'resource' : 'provenance', id);
    if (atTime)
        params.append('at', atTime);

    const trail: Trail<TrailType> = await (await fetch(`/trail?${params.toString()}`)).json();

    createTree(trail);
    writeMetadata(trail);
}

function createTree(trail: Trail<TrailType>) {
    const rect = document.querySelector('.container')!.getBoundingClientRect();
    const minWidth = Math.floor(rect.width);
    const height = 400;

    const leftHierarchy = hierarchy(trail.sourceRoot, d => d.relations);
    const rightHierarchy = hierarchy(trail.targetRoot, d => d.relations);

    const totalHeight = leftHierarchy.height + rightHierarchy.height;
    const width = Math.max(minWidth, totalHeight * 150);

    const svg = select('.tree');
    svg.attr('width', width)
        .attr('height', height)
        .selectAll('*')
        .remove();

    svg.append('defs')
        .append('marker')
        .attr('id', 'arrowhead')
        .attr('markerWidth', 8)
        .attr('markerHeight', 6)
        .attr('refX', 13)
        .attr('refY', 3)
        .attr('orient', 'auto')
        .attr('fill', '#ccc')
        .append('polygon')
        .attr('points', '0,0 8,3 0,6');

    const leftWidth = ((width - 20) / totalHeight) * leftHierarchy.height;
    const rightWidth = ((width - 20) / totalHeight) * rightHierarchy.height;

    const leftTree = tree<TrailNode<TrailType>>().size([height - 20, -leftWidth]);
    const rightTree = tree<TrailNode<TrailType>>().size([height - 20, rightWidth]);

    const leftNodes = leftTree(leftHierarchy);
    const rightNodes = rightTree(rightHierarchy);

    const main = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(10, 10)');

    const leftMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);
    const rightMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);

    for (const isLeft of [true, false]) {
        const nodes = isLeft ? leftNodes : rightNodes;
        const main = isLeft ? leftMain : rightMain;

        nodes.x = height / 2;

        main.append('g')
            .attr('fill', 'transparent')
            .attr('stroke', '#ccc')
            .attr('stroke-width', 2)
            .attr(isLeft ? 'marker-end' : 'marker-start', 'url(#arrowhead)')
            .selectAll('path')
            .data(nodes.descendants().slice(1))
            .join('path')
            .attr('d', d =>
                `M${d.y},${d.x}` +
                `L${d.y + 25},${d.x}` +
                `C${(d.y + d.parent?.y!) / 2},${d.x}` +
                ` ${(d.y + d.parent?.y!) / 2},${d.parent?.x}` +
                ` ${d.parent?.y! - 25},${d.parent?.x}` +
                `L${d.parent?.y},${d.parent?.x}`
            );

        main.append('g')
            .attr('fill', 'transparent')
            .attr('stroke', '#ccc')
            .attr('stroke-width', 1)
            .attr('stroke-dasharray', 4)
            .selectAll('path')
            .data(nodes.descendants().filter(d => isResource(d.data) && d.data.provIdUpdate != null))
            .join('path')
        // .attr('d', d => {
        //     let cur = d.parent;
        //     while (cur != null && cur.data.id !== d.data.provIdUpdate)
        //         cur = d.parent?.parent;
        //
        //     if (cur !== null)
        //         cur = cur.parent;
        //
        //     if (cur != null)
        //         return `M${d.y},${d.x}` +
        //             `C${d.y},${d.x - 50}` +
        //             ` ${cur.y},${cur.x - 50}` +
        //             ` ${cur.y},${cur.x}`
        //
        //     return '';
        // });

        const resourceNode = main.append('g')
            .selectAll('g')
            .data(nodes.descendants().filter(d => isResource(d.data)))
            .join('g')
            .attr('transform', d => `translate(${d.y},${d.x})`);

        resourceNode.append('circle')
            .attr('r', 8)
            .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            .attr('stroke', color)
            .attr('stroke-width', 2)
            .filter(d => d.depth !== 0)
            .style('cursor', 'pointer')
            .on('mouseover', (e, d) => {
                const elem = document.getElementsByClassName('resource-info')[0] as HTMLElement;
                elem.innerText = (d.data as Resource).resource;
            })
            .on('mouseout', _ => {
                const elem = document.getElementsByClassName('resource-info')[0] as HTMLElement;
                elem.innerText = '';
            })
            .on('click', (e, d) => {
                const dates = [];
                if (d.parent && isProvenance(d.parent.data))
                    dates.push(Object.values(d.parent.data.records));
                if (d.children)
                    d.children.forEach(d =>
                        dates.push(Object.values((d.data as Provenance).records)));
                dates.sort();

                window.location.hash = dates.length > 0
                    ? `${(d.data as Resource).resource};@${dates[0]}`
                    : `${(d.data as Resource).resource}`;
                e.preventDefault();
            });

        resourceNode
            .filter(d => d.depth === 0)
            .attr('class', 'selected');

        const provenanceNode = main.append('g')
            .selectAll('g')
            .data(nodes.descendants().filter(d => d.data.type === 'provenance'))
            .join('g')
            .attr('transform', d => `translate(${d.y},${d.x})`);

        provenanceNode.append('rect')
            .attr('width', 14)
            .attr('height', 14)
            .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            .attr('stroke', color)
            .attr('stroke-width', 2)
            .attr('transform', 'translate(0, -10) rotate(45)')
            .filter(d => d.depth !== 0);
        // .style('cursor', 'pointer')
        // .on('click', (e, d) => {
        //     window.location.hash = `${d.data.combinedId}`;
        //     e.preventDefault();
        // });

        provenanceNode
            .filter(d => d.depth === 0)
            .attr('class', 'selected');

        // provenanceNode.append('text')
        //     .attr('y', -5)
        //     .attr('x', d => !d.children && !isLeft ? -13 : 13)
        //     .attr('fill', color)
        //     .attr('text-anchor', d => !d.children && !isLeft ? 'end' : 'start')
        //     .text(d => `#${d.data.combinedId}`);

        document.getElementsByClassName('selected')[0].scrollIntoView({
            behavior: 'smooth',
            block: 'nearest',
            inline: 'center'
        });
    }
}

async function writeMetadata(trail: Trail<TrailType>) {
    const root = document.getElementsByClassName('metadata')[0];
    while (root.firstChild)
        root.removeChild(root.lastChild!);

    const header = document.createElement('h1');
    const links = document.createElement('dl');
    root.append(header, links);

    if (isResource(trail.sourceRoot)) {
        header.innerText = trail.sourceRoot.resource;

        if (trail.sourceRoot.relations.length > 0)
            writeData(links, 'Is the result of events with provenance identifier',
                trail.sourceRoot.relations.flatMap(source => Object.keys(source.records).map(rec => createProvenanceElem(root, rec))));

        if (isResource(trail.targetRoot) && trail.targetRoot.relations.length > 0)
            writeData(links, 'Is used as input for events with provenance identifier',
                trail.targetRoot.relations.flatMap(target => Object.keys(target.records).map(rec => createProvenanceElem(root, rec))));
    }
    else if (isProvenance(trail.sourceRoot)) {
        header.innerText = 'Event with provenance ids ' + Object.keys(trail.sourceRoot.records).map(id => `#${id}`).join(',');

        if (trail.sourceRoot.relations.length > 0)
            writeData(links, 'The event used the following resources as input',
                trail.sourceRoot.relations.map(source => createResourceElem(root, source)));

        if (trail.targetRoot.relations.length > 0)
            writeData(links, 'Which resulted in the following resources',
                trail.targetRoot.relations.map(target => createResourceElem(root, target as Resource)));

        const provHeader = document.createElement('h2');
        provHeader.innerText = 'Provenance';

        const prov = document.createElement('dl');
        root.append(provHeader, prov);

        provHeader.className = '';
        document.querySelectorAll(`[data-prov-id="${trail.sourceRoot.combinedId}"]`)
            .forEach(link => link.setAttribute('stroke', '#ccc'));

        while (prov.firstChild)
            prov.removeChild(prov.lastChild!);

        // prov.dataset.id = trail.sourceRoot.combinedId;

        document.querySelectorAll(`[data-prov-id="${trail.sourceRoot.combinedId}"]`)
            .forEach(link => link.setAttribute('stroke', '#4682b4'));

        const record = await (await fetch(`/prov/${trail.sourceRoot.combinedId}`)).json();
        if (record.who) writeData(prov, 'Who', record.who);
        if (record.where) writeData(prov, 'Where', record.where);
        if (record.when) writeData(prov, 'When', record.when);
        if (record.howSoftware || record.howInit) {
            if (record.howSoftware && record.howInit)
                writeData(prov, 'How', record.howSoftware + ' ' + record.howInit);
            else if (record.howSoftware)
                writeData(prov, 'How', record.howSoftware);
            else
                writeData(prov, 'How', record.howInit);
        }
        if (record.whyMotivation) writeData(prov, 'Why', record.whyMotivation);
    }
}

function createProvenanceElem(root: Element, id: string) {
    const resourceLink = document.createElement('a');
    resourceLink.innerText = '#' + id;
    resourceLink.setAttribute('href', `#${id}`);

    const resourceElem = document.createElement('dd');
    resourceElem.append(resourceLink);

    return resourceElem;
}

function createResourceElem(root: Element, data: Resource) {
    const resourceLink = document.createElement('a');
    resourceLink.innerText = data.resource;
    resourceLink.setAttribute('href', `#${data.resource}`);

    const resourceElem = document.createElement('dd');
    resourceElem.append(resourceLink);

    return resourceElem;
}

function writeData(parent: Element, header: string, values: string | (string | Element)[]) {
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

'use strict';

let provId;

window.addEventListener('hashchange', withProvRecord);
withProvRecord();

async function withProvRecord() {
    provId = window.location.hash.substring(1);
    const [record, trail] = await Promise.all([
        (await fetch(`/api/${provId}`)).json(),
        (await fetch(`/api/${provId}/trail`)).json()]);

    createTree(trail);
    writeRecord(record);
}

function createTree(trail) {
    const nodes = [...new Set(trail.flatMap(rel => [
        rel.source.resource,
        rel.target.resource,
        `prov_${rel.id}`
    ]))].map(res => ({id: res, isResource: !res.startsWith('prov_'), isCurRecord: res === `prov_${provId}`}));

    const links = trail.flatMap(rel => [
        {source: rel.source.resource, target: `prov_${rel.id}`},
        {source: `prov_${rel.id}`, target: rel.target.resource},
    ]);

    const rect = document.querySelector('.container').getBoundingClientRect();
    const width = Math.floor(rect.width);
    const height = 500;

    const svg = d3.select('#tree');
    svg.attr('width', width)
        .attr('height', height)
        .selectAll('*')
        .remove();

    svg.append('defs').append('marker')
        .attr('id', 'arrow')
        .attr('viewBox', '0 -5 10 10')
        .attr('refX', 15)
        .attr('refY', 0)
        .attr('markerWidth', 10)
        .attr('markerHeight', 10)
        .attr('orient', 'auto')
        .append('path')
        .attr('fill', 'black')
        .attr('d', 'M0,-5L10,0L0,5');

    const link = svg.append('g')
        .attr('stroke-width', 1)
        .attr('stroke', 'black')
        .attr('marker-end', 'url(#arrow)')
        .selectAll('line')
        .data(links)
        .join('line');

    const node = svg.append('g')
        .attr('fill', 'black')
        .attr('stroke', 'transparent')
        .attr('stroke-width', 1.5)
        .attr('stroke-linecap', 'round')
        .attr('stroke-linejoin', 'round')
        .attr('font-family', 'sans-serif')
        .attr('font-size', 10)
        .selectAll('g')
        .data(nodes)
        .join('g')
        .attr('title', d => d.id);

    node.filter(d => d.isResource)
        .append('title')
        .text(d => d.id);

    node.append('circle')
        .attr('stroke', 'black')
        .attr('stroke-width', 1)
        .attr('r', d => d.isCurRecord ? 12 : 6)
        .attr('fill', d => d.isResource ? 'black' : (d.isCurRecord ? '#F1F1F1' : 'white'))
        .on('mouseover', function (e, d) {
            if (!d.isResource)
                d3.select(this).style('cursor', 'pointer');
        })
        .on('mouseout', function (e, d) {
            if (!d.isResource)
                d3.select(this).style('cursor', 'default');
        })
        .on('click', (e, d) => {
            if (!d.isResource) {
                window.location.hash = d.id.replace('prov_', '');
                e.preventDefault();
            }
        });

    node.filter(d => d.isResource)
        .append('text')
        .attr('x', 12)
        .attr('dy', '.35em')
        .text(d => {
            if (d.isResource) {
                if (d.id.length <= 30) return d.id;
                return '...' + d.id.substring(d.id.length - 30);
            }
        });

    d3.forceSimulation(nodes)
        .force('link', d3.forceLink(links).id(node => node.id))
        .force('charge', d3.forceManyBody().strength(-400))
        .force('center', d3.forceCenter(width / 2, height / 2))
        .force('collide', d3.forceCollide().radius(d => d.isCurRecord ? 12 : 6))
        .force('x', d3.forceX().x(d => d.isCurRecord ? Math.floor(width / 2) : 0))
        .force('y', d3.forceY().y(d => !d.isResource ? Math.floor(height / 2) : 0 /*Math.floor(height / 2)*/))
        //.on('tick', _ => drawTree(svg, width, height, nodes, links));
        .on('tick', _ => {
            link.attr('x1', d => d.source.x)
                .attr('y1', d => d.source.y)
                .attr('x2', d => d.target.x)
                .attr('y2', d => d.target.y);

            node.attr('transform', d => `translate(${d.x},${d.y})`);
        });
}

// function drawTree(ctx, width, height, nodes, links) {
//     ctx.save();
//     ctx.clearRect(0, 0, width, height);
//
//     ctx.fillStyle = '#F1F1F1';
//     ctx.fillRect(0, 0, width, height);
//     ctx.fill();
//
//     for (const link of links) {
//         ctx.beginPath();
//         ctx.moveTo(link.source.x, link.source.y);
//         ctx.lineTo(link.target.x, link.target.y);
//         ctx.closePath();
//
//         ctx.lineWidth = 1;
//         ctx.strokeStyle = 'black';
//         ctx.stroke();
//
//         const radians = Math.atan((link.target.y - link.source.y) / (link.target.x - link.source.x))
//             + ((link.target.x > link.source.x) ? 90 : -90) * Math.PI / 180;
//
//         ctx.save();
//         ctx.beginPath();
//         ctx.translate(link.target.x, link.target.y);
//         ctx.rotate(radians);
//         ctx.moveTo(0, 0);
//         ctx.lineTo(3, 10);
//         ctx.lineTo(-3, 10);
//         ctx.closePath();
//         ctx.restore();
//
//         ctx.fillStyle = 'black';
//         ctx.fill();
//     }
//
//     for (const node of nodes) {
//         const size = node.isCurRecord ? 12 : 6;
//
//         ctx.beginPath();
//         ctx.arc(node.x, node.y, size, 0, 2 * Math.PI, false);
//
//         ctx.lineWidth = 1;
//         ctx.strokeStyle = 'black';
//         ctx.stroke();
//
//         ctx.fillStyle = node.isResource ? 'black' : (node.isCurRecord ? '#F1F1F1' : 'white');
//         ctx.fill();
//
//         if (node.isResource) {
//             ctx.font = '12px sans-serif';
//             ctx.fillStyle = 'black';
//             ctx.fillText(node.id.substring(0, 30), node.x + 10, node.y + 10);
//         }
//     }
//
//     ctx.restore();
// }

function writeRecord(record) {
    const dl = document.querySelector('#record dl');
    while (dl.firstChild)
        dl.removeChild(dl.lastChild);

    writeRelation(dl, 'From files', record.source);
    writeRelation(dl, 'Resulting files', record.target);

    if (record.who) writeData(dl, 'Who', record.who);
    if (record.where) writeData(dl, 'Where', record.where);
    if (record.when) writeData(dl, 'When', record.when);
    if (record.how) writeData(dl, 'How', record.how);
    if (record.why) writeData(dl, 'Why', record.why);
}

function writeRelation(parent, header, relations) {
    const headerElem = document.createElement('dt');
    headerElem.innerText = header;

    const valueElems = relations.map(rel => {
        const valueElem = document.createElement('dd');
        valueElem.innerText = `${rel.resource} (${rel.relation})`;
        return valueElem;
    })

    parent.append(headerElem, ...valueElems);
}

function writeData(parent, header, value) {
    const headerElem = document.createElement('dt');
    headerElem.innerText = header;

    const valueElem = document.createElement('dd');
    valueElem.innerText = value;

    parent.append(headerElem, valueElem);
}

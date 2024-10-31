import {select, hierarchy, tree} from 'd3';
import {TrailType, isResource, Resource, Provenance, isProvenance} from './interfaces.ts';

const color = '#4682b4';
const selectedColor = '#cfdeec';

export default function createGraph(elem: HTMLElement, trailSources: Resource, trailTargets?: Resource) {
    createLegend(elem);
    return createTree(elem, trailSources, trailTargets);
}

function createLegend(elem: HTMLElement) {
    const svg = select(elem.querySelector('.legend'));
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

function createTree(elem: HTMLElement, trailSources: Resource, trailTargets?: Resource) {
    const rect = elem.getBoundingClientRect();
    const minWidth = Math.floor(rect.width);
    const height = 400;

    const leftHierarchy = hierarchy<TrailType>(trailSources, d => d.relations);
    const rightHierarchy = trailTargets ? hierarchy<TrailType>(trailTargets, d => d.relations) : null;

    const totalHeight = leftHierarchy.height + (rightHierarchy?.height || 0);
    const width = Math.max(minWidth, totalHeight * 150);

    const svg = select(elem.querySelector('.tree'));
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
    const rightWidth = ((width - 20) / totalHeight) * (rightHierarchy?.height || 0);

    const leftTree = tree<TrailType>().size([height - 20, -leftWidth]);
    const rightTree = tree<TrailType>().size([height - 20, rightWidth]);

    const leftNodes = leftTree(leftHierarchy);
    const rightNodes = rightHierarchy && rightTree(rightHierarchy);

    const main = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(10, 10)');

    const leftMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);
    const rightMain = main.append('g').attr('transform', `translate(${leftWidth}, 0)`);

    for (const isLeft of [true, false]) {
        const nodes = isLeft ? leftNodes : rightNodes;
        const main = isLeft ? leftMain : rightMain;

        if (nodes) {
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
                .on('mouseover', (_, d) => {
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

                    const params = new URLSearchParams();
                    params.append('resource', (d.data as Resource).resource);
                    if (dates.length > 0)
                        params.append('at', dates[0].toString());
                    window.location.search = params.toString();

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

            document.getElementsByClassName('selected')[0].scrollIntoView({
                behavior: 'smooth',
                block: 'nearest',
                inline: 'center'
            });
        }
    }
}

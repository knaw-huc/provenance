import {select, hierarchy, tree} from 'd3';
import {INode, IProvenance, IResource, isProvenance, isResource} from "./interfaces.ts";
// import {TrailType, isResource, Resource, Provenance, isProvenance} from './interfaces.ts';

const color = '#4682b4';
const selectedColor = '#cfdeec';

export default function createGraph(elem: HTMLElement, trailSources: INode, trailTargets?: INode) {
    createLegend(elem);
    return createTree(elem, trailSources, trailTargets);
}

function createLegend(elem: HTMLElement) {
    const svg = select(elem.querySelector('.legend'));
    svg.attr('width', 150)
        .attr('height', 80)
        .selectAll('*')
        .remove();

    const resourceGroup = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(15, 10)');

    resourceGroup.append('use')
        .attr('xlink:href', '#IconAction')
        .attr('transform', 'translate(-8, 0), scale(.8)')
        // .attr('cx', 0)
        // .attr('cy', 10)
        // .attr('r', 8)
        // .attr('fill', '#fff')
        // .attr('stroke', color)
        // .attr('stroke-width', 2);

    resourceGroup.append('text')
        .attr('x', 30)
        .attr('y', 15)
        .attr('text-anchor', 'start')
        .text('Resource');

    const provGroup = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(15, 45)');

    provGroup.append('use')
        .attr('xlink:href', '#IconDoc')
        .attr('transform', 'translate(-8, 0), scale(.8)')
        // .attr('x', 0)
        // .attr('y', 0)
        // .attr('width', 14)
        // .attr('height', 14)
        // .attr('fill', '#fff')
        // .attr('stroke', color)
        // .attr('stroke-width', 2)
        // .attr('transform', 'rotate(45)');

    provGroup.append('text')
        .attr('x', 30)
        .attr('y', 15)
        .attr('text-anchor', 'start')
        .text('Provenance event');
}

function createTree(elem: HTMLElement, trailSources: INode) {
    const rect = elem.getBoundingClientRect();
    const minWidth = Math.floor(rect.width);
    console.log('minWidth', minWidth);
    const minHeight = 400;

    const treeHierarchy = hierarchy<INode>(trailSources, d => d.relations);

    console.log("treeHierarchy", treeHierarchy);

    const width = minWidth;
    const height = Math.max(minHeight, treeHierarchy.height * 80);

    const svg = select(elem.querySelector('.tree'));
    svg.attr('width', width)
        .attr('height', height + 50)
        .selectAll('*')
        .remove();

    const defs = svg.append('defs')

    defs.append('marker')
        .attr('id', 'arrowhead')
        .attr('markerWidth', 8)
        .attr('markerHeight', 6)
        .attr('refX', 13)
        .attr('refY', 3)
        .attr('orient', 'auto')
        .attr('fill', '#ccc')
        .append('polygon')
        .attr('points', '0,0 8,3 0,6');

    // const iconDocTop = defs.append('g')
    //     .attr('id', 'IconDocTop')
    //
    // iconDocTop.append('rect')
    //     .attr('x', 0)
    //     .attr('y', 0)
    //     .attr('width', '26px')
    //     .attr('height', '26px')
    //     .attr('class', 'fill-republicOrange-400 rounded')
    //     .attr('rx', 5)
    //     .attr('ry', 5)


    const leftTree = tree<INode>().size([width, height]);

    const leftNodes = leftTree(treeHierarchy);

    const main = svg.append('g')
        .attr('font-family', 'sans-serif')
        .attr('font-size', '8pt')
        .attr('transform', 'translate(10, 10)');

    const nodes = leftNodes;

    if (nodes) {
        nodes.x = width / 2;

        main.append('g')
            .attr('fill', 'transparent')
            .attr('stroke', '#ccc')
            .attr('stroke-width', 2)
            // .attr('marker-end', 'url(#arrowhead)')
            .selectAll('path')
            .data(nodes.descendants().slice(1))
            .join('path')
            .attr('d', d =>
                `M${d.x},${d.y}` +
                // `L${d.x},${d.y - 25}` +
                `C${d.x},${d.parent?.y}` +
                ` ${d.parent?.x!},${d.y}` +
                ` ${d.parent?.x!},${d.parent?.y}`// +
                // `L${d.parent?.x},${d.parent?.y}`
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
            .attr('transform', d => `translate(${d.x},${d.y})`);

        resourceNode.append('use')
            .attr('xlink:href', d => d.depth === 0 ? '#IconDocTop' : '#IconDoc')
            .attr('transform', 'translate(-8, -8), scale(.8)')
            // .attr('r', 8)
            // .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            // .attr('stroke', color)
            // .attr('stroke-width', 2)
            .filter(d => d.depth !== 0)
            .style('cursor', 'pointer')
            .on('mouseover', (_, d) => {
                const elem = document.getElementsByClassName('resource-info')[0] as HTMLElement;
                elem.innerText = (d.data as IResource).resource;
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
                        dates.push(Object.values((d.data as IProvenance).records)));
                dates.sort();

                const params = new URLSearchParams();
                params.append('resource', (d.data as IResource).resource);
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
            .attr('transform', d => `translate(${d.x},${d.y})`);

        provenanceNode.append('use')
            .attr('xlink:href', '#IconAction')
            .attr('transform', 'translate(-8, -8), scale(.8)')
            // .attr('width', 14)
            // .attr('height', 14)
            // .attr('fill', d => d.depth === 0 ? selectedColor : '#fff')
            // .attr('stroke', color)
            // .attr('stroke-width', 2)
            // .attr('transform', 'translate(0, -10) rotate(45)')
            .filter(d => d.depth !== 0);

        document.getElementsByClassName('selected')[0].scrollIntoView({
            behavior: 'smooth',
            block: 'nearest',
            inline: 'center'
        });
    }
}

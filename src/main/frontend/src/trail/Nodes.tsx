import {INode, IProvenance, IResource, ITemplate} from "../interfaces.ts";
import {ResourcesNode} from "./ResourcesNode.tsx";
import {useState} from "react";
import {Provenances} from "./Provenances.tsx";


export function Nodes({nodes, templates}: {nodes: INode[], templates: ITemplate[]}) {

    const [selectedNode, setSelectedNode] = useState<INode>(nodes[0]);

    function renderNodes() {
        switch (selectedNode.type) {
            case 'provenance':
                // return <ProvenanceNode provenance={selectedNode as IProvenance} templates={templates} />
                return <Provenances provenances={nodes as IProvenance[]} templates={templates} selectNode={(node) => {setSelectedNode(node)}} />
            default:
                return <ResourcesNode resources={nodes as IResource[]} selectNode={(node) => {setSelectedNode(node)}} />
        }
    }

    function renderRelation() {
        if (selectedNode.relations.length == 0) {
            return null
        }
        return <Nodes nodes={selectedNode.relations} templates={templates} />
    }

    return <>
        {renderNodes()}
        {renderRelation()}
    </>
}

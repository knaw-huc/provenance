import {NodeType} from "./enums.ts";

export type SelectNodeFunction = (node: INode) => void

export interface ITemplate {
    provenance: string
    value: string
    isRegex: boolean
    description: string
}

export interface INode {
    type: NodeType
    relation: string
    relations: INode[]
}

export interface IProvenanceData {
    who: string
    where: string
    when: string
    howSoftware: string
    howInit: string
    howDelta: string
    whyMotivation: string
    whyProvenanceSchema: string
}

export interface IProvenance extends INode {
    type: NodeType.Provenance
    data: IProvenanceData
    combinedId: number
    records: any[]
}

export interface IResource extends INode {
    type: NodeType.Resource
    resource: string
    provIdUpdate: number
}

export function isResource(node: INode): node is IResource {
    return node.type === NodeType.Resource
}

export function isProvenance(node: INode): node is IProvenance {
    return node.type === NodeType.Provenance
}

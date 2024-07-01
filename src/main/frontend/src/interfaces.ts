export interface TrailNode<R> {
    type: 'provenance' | 'resource';
    relation: string;
    relations: TrailNode<R>[];
}

export interface Provenance extends TrailNode<Resource> {
    type: 'provenance';
    combinedId: number;
    records: { [provId: number]: Date };
    data: ProvenanceData;
    relations: Resource[];
}

export interface Resource extends TrailNode<Provenance> {
    type: 'resource';
    resource: string;
    provIdUpdate: string | null;
    relations: Provenance[];
}

export interface ProvenanceData {
    who: string;
    where: string;
    when: string;
    howSoftware: string;
    howInit: string;
    howDelta: string;
    whyMotivation: string;
    whyProvenanceSchema: string;
}

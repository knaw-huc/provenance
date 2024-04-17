import React, {createContext, ReactNode, useContext, useEffect, useState} from 'react';

export interface ProvenanceTemplate {
    provenance: string;
    value: string;
    isRegex: boolean;
    description: string;
}

export interface ProvenanceTemplates {
    [provenance: string]: ProvenanceTemplate[]
}

const ProvenanceTemplatesContext = createContext<ProvenanceTemplates>({});

export function ProvenanceTemplatesProvider({children}: { children?: ReactNode }) {
    const [provenanceTemplates, setProvenanceTemplates] = useState<ProvenanceTemplates>({});

    async function loadProvenanceTemplates() {
        const result = await fetch('/prov/templates');
        if (result.ok) {
            const templates = await result.json() as ProvenanceTemplate[];
            const templatesObj = templates.reduce<ProvenanceTemplates>((acc, template) => {
                if (!(template.provenance in acc)) {
                    acc[template.provenance] = [];
                }
                acc[template.provenance].push(template);
                return acc;
            }, {});
            setProvenanceTemplates(templatesObj);
        }
    }

    useEffect(() => {
        loadProvenanceTemplates();
    }, []);

    return (
        <ProvenanceTemplatesContext.Provider value={provenanceTemplates}>
            {children}
        </ProvenanceTemplatesContext.Provider>
    );
}

export function useProvenanceTemplateFor(provenance: string, value: string): ProvenanceTemplate | undefined {
    const provTemplates = useContext(ProvenanceTemplatesContext);
    const templates = provTemplates[provenance] || [];
    return templates.find(template =>
        !template.isRegex ? template.value === value : new RegExp(template.value).test(value));
}

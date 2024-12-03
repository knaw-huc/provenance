import {IResource, SelectNodeFunction} from "../interfaces.ts";
import {Resource} from "./Resource.tsx";
import React from "react";

export function ResourcesNode({resources, selectNode}: { resources: IResource[], selectNode: SelectNodeFunction }) {

    const [selectedResource, setSelectedResource] = React.useState<IResource>(resources[0]);

    function onClickResource (resource: IResource) {
        console.log("Click!")
        setSelectedResource(resource);
        selectNode(resource);
    }

    return <>
        <div className="group flex flex-col md:flex-row gap-6 border-b border-l-2 border-neutral-300 border-b-neutral-200/70 py-6 flex-wrap lg:flex-nowrap pl-6 md:pl-0 relative  after:content-['▲'] after:absolute after:bottom-0 after:text-neutral-300 after:text-xl after:-translate-x-2 after:translate-y-2 relative">
            <div className="absolute text-xs text-neutral-500 -translate-x-36 hidden group-hover:block">Resources</div>
            <div className="w-1/2 md:w-1/12">
                <div className="p-1 border-neutral-300 rotate-45 -translate-x-10 md:-translate-x-4 inline-block bg-white border">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                         stroke-width="1.5" stroke="currentColor" className="w-5 h-5 stroke-neutral-700 -rotate-45">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 0 1-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H6.75a9.06 9.06 0 0 1 1.5.124m7.5 10.376h3.375c.621 0 1.125-.504 1.125-1.125V11.25c0-4.46-3.243-8.161-7.5-8.876a9.06 9.06 0 0 0-1.5-.124H9.375c-.621 0-1.125.504-1.125 1.125v3.5m7.5 10.375H9.375a1.125 1.125 0 0 1-1.125-1.125v-9.25m12 6.625v-1.875a3.375 3.375 0 0 0-3.375-3.375h-1.5a1.125 1.125 0 0 1-1.125-1.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H9.75"
                        />
                    </svg>
                </div>
            </div>
            <div className="w-1/2 md:w-2/12 font-bold text-sm whitespace-nowrap "></div>
            <div className="w-9/12 columns-2">
                {resources.map((resource) => {
                    return <Resource key={resource.resource} selected={resource.resource == selectedResource.resource} resource={resource} onClick={() => onClickResource(resource)} />
                })}
            </div>
        </div>
    </>
}

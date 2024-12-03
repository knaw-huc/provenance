import {IResource, ITemplate} from "../interfaces.ts";
import {Nodes} from "./Nodes.tsx";

export function MainNode({trail, templates}: {trail: IResource, templates: ITemplate[]}) {

    console.log("MainNode trail: ", trail)
    const resourceParts = trail.resource.split('/')
    let name = resourceParts[resourceParts.length - 1]
    name = name.charAt(0).toUpperCase() + name.slice(1)

    return (
        <>
            <div className="group flex flex-col md:flex-row gap-6 border-b border-l-2 border-neutral-300 border-b-neutral-200/70 pb-16 flex-wrap lg:flex-nowrap pl-6 md:pl-0 relative  after:content-['▲'] after:absolute after:bottom-0 after:text-neutral-300 after:text-xl after:-translate-x-2 after:translate-y-2 relative">
                <div className="absolute text-xs text-neutral-500 -translate-x-36 hidden group-hover:block"></div>
                <div className="w-1/2 md:w-1/12">
                    <div className="p-1 border-neutral-300 rotate-45 -translate-x-10 md:-translate-x-4 inline-block bg-republicOrange-400">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                             strokeWidth="1.5" stroke="currentColor" className="w-5 h-5 stroke-neutral-700 -rotate-45">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"
                            />
                        </svg>
                    </div>
                </div>
                <div className="w-full md:w-11/12">

                    <div className="font-bold text-xl">{name}</div>
                    <a href={trail.resource}
                       className="text-sm text-neutral-500 after:content-['→'] after:pl-1 no-underline">{trail.resource}</a>
                </div>
            </div>
            <Nodes nodes={trail.relations} templates={templates} />
        </>
    )
}

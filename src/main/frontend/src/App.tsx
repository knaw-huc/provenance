import './tailwind.css'
import './App.css'
import {NavLink, Outlet} from "react-router-dom";


function App() {
    return (
        <>
            <header className="bg-rpBrand1-900 text-republicOrange-400  px-4">
                <div className="max-w-[1200px] w-full m-auto flex items-center gap-6">
                    <div className="py-3 border-r border-republicOrange-400/20">
                        <img src="logo-goetgevonden.png" className="h-12 pt-3 mr-6" alt=""
                             loading="lazy"/>
                    </div>
                    <div className="font-bold">Provenance</div>
                    <div className="">Session-1705-01-01-ordinaris-num-1</div>
                </div>
            </header>
            <div className="flex flex-col md:flex-row w-full max-w-[1200px] m-auto my-16">
                <div className="">
                    <div className="bg-neutral-100 flex flex-row md:flex-col px-4  rounded py-6">
                        <NavLink to={`/`} className="whitespace-nowrap no-underline text-left">Data trail</NavLink>
                        <NavLink to={`/flow`} className="whitespace-nowrap no-underline text-left">Flow</NavLink>
                    </div>
                </div>
                <main className="px-4 md:px-10 w-full">
                    <svg className="w-full h-full min-h-[800px] hidden" id="svgVis">
                        <defs>
                            <g id="IconDocTop">
                                {/*<rect x="0" y="0" width="26px" height="26px" className="fill-republicOrange-400 rounded"*/}
                                {/*      rx="5" ry="5"/>*/}
                                <rect x="1" y="-15" width="30px" height="30px"
                                      className="stroke-neutral-200 rotate-45 fill-republicOrange-400"
                                />
                                <path strokeLinecap="round" strokeLinejoin="round"
                                      className="stroke-neutral-700 fill-none"
                                      d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"
                                />
                            </g>
                            <g id="IconDoc">
                                <rect x="1" y="-15" width="30px" height="30px"
                                      className="stroke-neutral-200 rotate-45 fill-white"
                                />
                                <path strokeLinecap="round" strokeLinejoin="round" className="stroke-neutral-700 fill-none"
                                      d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"
                                />
                            </g>
                            <g id="IconAction">
                                <rect x="1" y="-15" width="30px" height="30px" className="stroke-neutral-200 rotate-45 fill-white"
                                />
                                <path strokeLinecap="round" strokeLinejoin="round" className="stroke-neutral-700 fill-none"
                                      d="M11.42 15.17L17.25 21A2.652 2.652 0 0021 17.25l-5.877-5.877M11.42 15.17l2.496-3.03c.317-.384.74-.626 1.208-.766M11.42 15.17l-4.655 5.653a2.548 2.548 0 11-3.586-3.586l6.837-5.63m5.108-.233c.55-.164 1.163-.188 1.743-.14a4.5 4.5 0 004.486-6.336l-3.276 3.277a3.004 3.004 0 01-2.25-2.25l3.276-3.276a4.5 4.5 0 00-6.336 4.486c.091 1.076-.071 2.264-.904 2.95l-.102.085m-1.745 1.437L5.909 7.5H4.5L2.25 3.75l1.5-1.5L7.5 4.5v1.409l4.26 4.26m-1.745 1.437l1.745-1.437m6.615 8.206L15.75 15.75M4.867 19.125h.008v.008h-.008v-.008z"
                                />
                            </g>
                        </defs>
                        <use xlinkHref="#IconDocTop" x="150" y="150" />
                        <use xlinkHref="#IconDoc" x="100" y="100" />
                        <use xlinkHref="#IconAction" x="50" y="50" />
                    </svg>
                    <div className="flex flex-col first:border-t relative" id="datatrail">
                        <Outlet />
                    </div>
                </main>
            </div>
        </>
    )
}

export default App

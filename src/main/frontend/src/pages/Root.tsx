import {NavLink, Outlet, useNavigation} from "react-router-dom";

export function Root() {

    const navigation = useNavigation();
    const isNavigating = Boolean(navigation.location);

    return <>
        <header className="bg-rpBrand1-900 text-republicOrange-400  px-4">
            <div className="max-w-[1200px] w-full m-auto flex items-center gap-6">
                <div className="py-3 border-r border-republicOrange-400/20">
                    <img src="logo-goetgevonden.png" className="h-12 pt-3 mr-6" alt=""
                         loading="lazy"/>
                </div>
                <NavLink to={'/'} className="font-bold">Provenance</NavLink>
            </div>
        </header>
        <div className="flex flex-col md:flex-row w-full max-w-[1200px] m-auto my-16">
            {isNavigating ? <p>Loading...</p> : <Outlet />}
        </div>
    </>
}

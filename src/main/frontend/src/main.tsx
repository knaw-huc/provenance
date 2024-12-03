import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import {createHashRouter, RouterProvider} from "react-router-dom";
import {TrailPage} from "./pages/TrailPage.tsx";
import {FlowPage} from "./pages/FlowPage.tsx";
import {Provenance, provenanceLoader} from "./pages/Provenance.tsx";
import {Root} from "./pages/Root.tsx";
import {Search} from "./pages/Search.tsx";
import {selectionLoader, SelectionPage} from "./pages/SelectionPage.tsx";

const router = createHashRouter([
    {
        path: '/',
        element: <Root />,
        children: [
            {
                index: true,
                element: <Search />
            },
            {
                path: "/target-selection/:provId",
                element: <SelectionPage />,
                loader: selectionLoader,
            },
            {
                path: "/:resource",
                element: <Provenance />,
                loader: provenanceLoader,
                children: [
                    {
                        path: "",
                        element: <TrailPage />
                    },
                    {
                        path: "flow",
                        element: <FlowPage />
                    }
                ]
            }
        ],
    },
])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)

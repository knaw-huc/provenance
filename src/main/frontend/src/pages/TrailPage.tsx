import {IResource, ITemplate} from "../interfaces.ts";
import {MainNode} from "../trail/MainNode.tsx";
import {useOutletContext} from "react-router-dom";

const templatesRaw: string = "[{\"provenance\":\"why_motivation\",\"value\":\"REPUBLIC CAF Pipeline deriving resolutions from session_lines\",\"isRegex\":false,\"description\":\"Deriving a resolution from session lines\"},{\"provenance\":\"where_location\",\"value\":\"https://annotation.republic-caf.diginfra.org/\",\"isRegex\":false,\"description\":\"REPUBLIC CAF server\"},{\"provenance\":\"how_software\",\"value\":\"^https:\\\\/\\\\/github\\\\.com\\\\/HuygensING\\\\/republic-project\\\\/commit\\\\/.+$\",\"isRegex\":true,\"description\":\"Importing, parsing, extracting and visualising information from the Resolutions of the Dutch States General\"},{\"provenance\":\"why_motivation\",\"value\":\"text extraction\",\"isRegex\":false,\"description\":\"Extraction of the text\"},{\"provenance\":\"where_location\",\"value\":\"file://lap1550/\",\"isRegex\":false,\"description\":\"Laptop of developer\"},{\"provenance\":\"how_software\",\"value\":\"^https:\\\\/\\\\/raw\\\\.githubusercontent\\\\.com\\\\/knaw-huc\\\\/un-t-ann-gle\\\\/.+$\",\"isRegex\":true,\"description\":\"Un-t-ann-gle\"}]"
const templates: ITemplate[] = JSON.parse(templatesRaw)

export function TrailPage() {
    const trail = useOutletContext<IResource>()
    if (trail == null) {
        return <></>
    }
    return <MainNode trail={trail} templates={templates} />
}

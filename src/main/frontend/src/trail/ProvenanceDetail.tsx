import {ITemplate} from "../interfaces.ts";
import {DetailType} from "../enums.ts";
import {formatValue} from "./formatting.ts";

const labels: {[key in DetailType]: string} = {
    [DetailType.Who]: 'Who',
    [DetailType.Where]: 'Where',
    [DetailType.When]: 'When',
    [DetailType.HowSoftware]: 'How',
    [DetailType.HowInit]: 'How',
    [DetailType.HowDelta]: 'How',
    [DetailType.WhyMotivation]: 'Why',
    [DetailType.WhyProvenanceSchema]: 'Why',
}

export function ProvenanceDetail ({type, value, templates}: {type: DetailType, value: string, templates: ITemplate[]}) {
    console.log("Value: ", value)

    if (value == null) {
        return <></>
    }

    return <>
        <div className="flex flex-col md:flex-row gap-6">
            <div className="w-1/2 md:w-2/12 font-bold text-sm whitespace-nowrap ">{labels[type]}</div>
            <div className="w-full md:w-9/12">{formatValue(value, type, templates)}</div>
        </div>
    </>
}

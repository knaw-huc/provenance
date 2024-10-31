import {DetailType} from "../enums.ts";
import {ITemplate} from "../interfaces.ts";

const formatLong: Intl.DateTimeFormatOptions = {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
}

function applyTemplate (value: string, templateType: string, templates: ITemplate[]) {
    for (const template of templates) {
        if (template.provenance != templateType) {
            continue
        }
        if (template.isRegex) {
            if (value.match(template.value) !== null) {
                return template.description
            }
            continue
        }
        if (value === template.value) {
            return template.description
        }
    }
    return value
}

export function formatValue(value: string, type: DetailType, templates: ITemplate[]) {
    switch (type) {
        case DetailType.Who:
            return applyTemplate(value, 'who', templates)
        case DetailType.Where:
            return applyTemplate(value, 'where_location', templates)
        case DetailType.When: {
            return new Date(value).toLocaleDateString("en-US", formatLong)
        }
        case DetailType.HowSoftware:
            return applyTemplate(value, 'how_software', templates)
        case DetailType.HowInit:
            return applyTemplate(value, 'how_init', templates)
        case DetailType.HowDelta:
            return applyTemplate(value, 'how_delta', templates)
        case DetailType.WhyMotivation:
            return applyTemplate(value, 'why_motivation', templates)
        case DetailType.WhyProvenanceSchema:
            return applyTemplate(value, 'why_provenance', templates)
    }
}

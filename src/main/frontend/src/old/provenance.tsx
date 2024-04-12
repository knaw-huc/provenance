import {useEffect, useRef, useState} from 'react';
import createGraph from './graph.js';
import './style.css';

export default function Provenance() {
    const initialized = useRef(false);

    useEffect(() => {
        if (!initialized.current) {
            initialized.current = true;
            createGraph();
        }
    }, []);

    return (
        <div className="container">
            <div className="tree-container">
                <svg className="tree"></svg>
                <svg className="legend"></svg>
                <div className="resource-info"></div>
            </div>
            <div className="metadata"></div>
        </div>
    );
}

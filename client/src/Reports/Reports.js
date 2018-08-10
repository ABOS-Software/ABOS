import React from 'react';
import {
    BooleanInput,
    fetchUtils,
    ImageField,
    ImageInput,
    ReferenceArrayInput,
    ReferenceInput,
    SelectArrayInput,
    SelectInput,
    SimpleForm,
    TextInput
} from 'react-admin';
import Wizard from './Wizard'
import download from 'downloadjs';

const steps = () => [
    "Pick Report Template", "Fill In Details"
];
const convertFileToBase64 = file => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file.rawFile);

    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
});

function save(record, redirect) {
    console.log(record);
    let options = {};
    let url = 'http://localhost:8080/api/Reports';
    if (!options.headers) {
        options.headers = new Headers({Accept: 'application/pdf'});
    }
    const token = localStorage.getItem('access_token');
    options.headers.set('Authorization', `Bearer ${token}`);
    if (record.LogoLocation) {
        convertFileToBase64(record.LogoLocation).then(b64 => {
                record.LogoLocation.base64 = b64;
                fetch(url, {
                    method: "POST",
                    mode: "cors",
                    cache: "no-cache",
                    credentials: "same-origin", // include, same-origin, *omit
                    headers: {
                        "Content-Type": "application/json; charset=utf-8",
                        'Authorization': `Bearer ${token}`
                        // "Content-Type": "application/x-www-form-urlencoded",
                    },
                    redirect: "follow", // manual, *follow, error
                    referrer: "no-referrer", // no-referrer, *client
                    body: JSON.stringify(record),
                }).then(response => response.blob())
                    .then(blob => download(blob, "report.pdf", "application/pdf"))
            }
        )
    } else {
        fetch(url, {
            method: "POST",
            mode: "cors",
            cache: "no-cache",
            credentials: "same-origin", // include, same-origin, *omit
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                'Authorization': `Bearer ${token}`
                // "Content-Type": "application/x-www-form-urlencoded",
            },
            redirect: "follow", // manual, *follow, error
            referrer: "no-referrer", // no-referrer, *client
            body: JSON.stringify(record),
        }).then(response => response.blob())
            .then(blob => download(blob, "report.pdf", "application/pdf"))
    }


    //console.log(fetchUtils.fetchJson(url, options));

}

const stepsContent = () => [
    <SelectInput
        source="template" choices={[{id: 'customers_split', name: 'Year; Split by Customer'}]}/>,
    [
        <TextInput
            source="Scout_name"/>,
        <TextInput
            source="Scout_address"/>,
        <TextInput
            source="Scout_Zip"/>,
        <TextInput
            source="Scout_Town"/>,
        <TextInput
            source="Scout_State"/>,
        <TextInput
            source="Scout_Phone"/>,
        <TextInput
            source="Scout_Rank"/>,
        <ImageInput
            source="LogoLocation" accept="image/*">
            <ImageField source="src" title="title"/>
        </ImageInput>,
        <ReferenceInput label="Year" source="Year" reference="Years">
            <SelectInput optionText="year" optionValue="id"/>
        </ReferenceInput>,

        <ReferenceInput label="User" source="User" reference="User">
            <SelectInput optionText="userName" optionValue="userName"/>
        </ReferenceInput>,

        <ReferenceArrayInput label="Customers" source="Customer" reference="customers">
            <SelectArrayInput optionText="customerName" optionValue="id" allowEmpty/>
        </ReferenceArrayInput>,

        <ReferenceInput label="Category" source="Category" reference="Categories" allowEmpty>
            <SelectInput optionText="categoryName" optionValue="categoryName" allowEmpty/>
        </ReferenceInput>,

        <BooleanInput
            source="Print_Due_Header"/>,

    ]
];

export const Reports = (props) => (
    <Wizard {...props} steps={steps()} stepContents={stepsContent()} save={save} formName={"Record-form"}/>
);


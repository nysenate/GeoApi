import React from 'react'
import { removeJobFile, submitJobRequest, uploadJobFile } from 'app/apis/jobApi'
import { yesNo } from 'app/views/job/jobUtils'

const ALLOWED_EXTENSIONS = [ 'tsv', 'txt', 'csv' ]

/**
 * "New batch job" pane: uploads files into the session's job request and
 * submits them all for processing. The upload queue lives server-side in the
 * session; this pane mirrors it in local state for display.
 */
export default function JobUpload({ visible, onSubmitted }) {
  const [ processes, setProcesses ] = React.useState([])
  const [ uploadProgress, setUploadProgress ] = React.useState(0)

  const uploadFiles = async (files) => {
    for (const file of files) {
      if (!isAllowedFile(file.name)) {
        window.alert(`Sorry, only ${ALLOWED_EXTENSIONS.join(', ')} files are allowed for batch processing.`)
        continue
      }
      try {
        const response = await uploadJobFile(file, (progress) =>
          setUploadProgress(progress < 1 ? progress : 0))
        if (response.success) {
          setProcesses((prev) => [ ...prev, response.jobProcess ])
        } else {
          window.alert(response.error ?? response.message ?? 'Server did not respond to upload request.')
        }
      } catch (e) {
        window.alert(e.message)
      } finally {
        setUploadProgress(0)
      }
    }
  }

  const onFilesSelected = (event) => {
    const files = [ ...event.target.files ]
    event.target.value = ''
    uploadFiles(files)
  }

  const removeFile = async (fileName) => {
    try {
      const response = await removeJobFile(fileName)
      if (response.success) {
        setProcesses((prev) => prev.filter((process) => process.fileName !== fileName))
      }
      window.alert(response.message)
    } catch (e) {
      window.alert('Failed to remove file from request.')
    }
  }

  const submit = async (event) => {
    event.preventDefault()
    try {
      const response = await submitJobRequest()
      if (response.success) {
        window.alert('Your request has been submitted')
        setProcesses([])
        onSubmitted()
      } else {
        window.alert(response.message)
      }
    } catch (e) {
      window.alert('Failed to submit batch job request!')
    }
  }

  return (
    <div id="upload-container" style={{ display: visible ? '' : 'none', width: '100%', height: '100%' }}>
      <form id="uploadForm" onSubmit={submit} style={{ width: '95%', margin: 'auto' }}>
        <ol>
          <li>
            <h3 style={{ color: '#333' }}>Upload files for processing</h3>
          </li>
          <li>
            <div id="fileuploaded" style={{ padding: '20px' }}>
              <div>
                <table className="job-table">
                  <thead style={{ textAlign: 'left', borderBottom: '1px solid #999' }}>
                  <tr>
                    <th width="450px">File name</th>
                    <th>USPS Validate</th>
                    <th>Geocode</th>
                    <th>District Assign</th>
                    <th>Record Count</th>
                    <th>Actions</th>
                  </tr>
                  </thead>
                  <tbody>
                  {processes.map((process) => (
                    <tr key={process.fileName}>
                      <td>{process.sourceFileName}</td>
                      <td>{yesNo(process.validationRequired)}</td>
                      <td>{yesNo(process.geocodeRequired)}</td>
                      <td>{yesNo(process.districtRequired)}</td>
                      <td>{process.recordCount}</td>
                      <td>
                        <div className="cancel" onClick={() => removeFile(process.fileName)}>Remove</div>
                      </td>
                    </tr>
                  ))}
                  {processes.length === 0 &&
                    <tr>
                      <td colSpan="4">
                        <p style={{ color: '#444' }}>The upload queue is empty. Use the Upload File
                          button below to add files.</p>
                      </td>
                    </tr>
                  }
                  </tbody>
                </table>
              </div>
              <div style={{ marginTop: '10px' }}>
                <label id="fileUploaderBasic">
                  <div className="icon-upload icon-teal"></div> Upload a file
                  <input id="fileUploaderInput" type="file" multiple accept=".tsv,.txt,.csv"
                         onChange={onFilesSelected}/>
                </label>
                <div id="fileUploadProgress"
                     style={{
                       visibility: uploadProgress > 0 ? 'visible' : 'hidden',
                       width: `${uploadProgress * 100}%`,
                     }}>&nbsp;</div>
              </div>
            </div>
          </li>
          <li className="submit-li">
            <label>&nbsp;</label>
            <button type="submit" className="submit" disabled={processes.length === 0}>
              <span id="form_submit">Submit Batch Request</span>
            </button>
          </li>
        </ol>
      </form>
      <JobInstructions />
    </div>
  )
}

function isAllowedFile(fileName) {
  const extensionIndex = fileName.lastIndexOf('.')
  const extension = extensionIndex > -1 ? fileName.substring(extensionIndex + 1).toLowerCase() : ''
  return ALLOWED_EXTENSIONS.includes(extension)
}

const ADDRESS_COLUMNS = [
  [ 'Street', 'street | streetAddress' ],
  [ 'City', 'city' ],
  [ 'State', 'state | stateProvinceId' ],
  [ 'Zip5', 'zip5 | zip | postal | postalCode' ],
  [ 'Zip4', 'zip4 | postalSuffix | postalCodeSuffix' ],
]

const USPS_ADDRESS_COLUMNS = [
  [ 'USPS Street', 'uspsStreetAddress | uspsStreet' ],
  [ 'USPS City', 'uspsCity' ],
  [ 'USPS State', 'uspsState' ],
  [ 'USPS Zip5', 'uspsZip5' ],
  [ 'USPS Zip4', 'uspsZip4' ],
]

const GEOCODE_COLUMNS = [
  [ 'Latitude', 'latitude | lat | geoCode1' ],
  [ 'Longitude', 'longitude | lon | lng | geoCode2' ],
  [ 'Geocode Method', 'geoMethod | geoSource' ],
  [ 'Geocode Quality', 'geoQuality | accuracy' ],
]

const DISTRICT_COLUMNS = [
  [ 'Senate', 'senate | nySenateDistrict47 | sd | senateDistrict' ],
  [ 'Assembly', 'assembly | nyAssemblyDistrict48 |  ad | assemblyDistrict' ],
  [ 'Congressional', 'congressional | congressionalDistrict46 | cd | congressionalDistrict' ],
  [ 'Town', 'town | town52 | townCode' ],
  [ 'School', 'school | schoolDistrict54 | schoolDistrict' ],
  [ 'Ward', 'ward | ward53 | wardCode' ],
  [ 'Election', 'election | electionDistrict49 | electionDistrict | ed' ],
]

/** The file formatting guidelines shown under the upload form, from jobmain.jsp. */
function JobInstructions() {
  return (
    <div id="jobInstructions" style={{ fontSize: '14px' }}>
      <h3>Guidelines</h3>
      <p>The job processor performs bulk geocoding and district assignment given a list of addresses.</p>
      <p>The file that is uploaded must be formatted in order to be processed successfully.</p>
      <br/>
      <p><strong>Supported formats</strong> - .tsv | .csv | .txt</p>
      <p><strong>Supported delimiters</strong> - tab | comma | semi-colon</p>
      <br/>
      <p>The following tables list the columns that are processed. The columns are read from the first line of the file.</p>
      <p>The aliases are the names that can be used to represent the column. The aliases are shown camelCased but </p>
      <p>they may also be underscored e.g streetAddress = street_address</p>
      <br/>
      <hr style={{ width: '500px', outline: 0, border: 0, borderTop: '1px solid #ddd' }}/>
      <p>Each row must contain an address. If the address is not parsed it can be defined by just the Street column.</p>
      <br/>
      <p><strong style={{ color: 'teal' }}>Address columns</strong></p>
      <AliasTable rows={ADDRESS_COLUMNS} />
      <br/>
      <p>If USPS address columns are specified, the processor will perform address correction.</p>
      <p>The Address columns above are used as input and the USPS Address columns will contain the corrected address.</p>
      <br/>
      <p><strong style={{ color: 'teal' }}>USPS Address columns</strong></p>
      <AliasTable rows={USPS_ADDRESS_COLUMNS} />
      <br/>
      <p>If geocode columns are specified, the processor will perform geocoding.</p>
      <br/>
      <p><strong style={{ color: 'teal' }}>Geocode columns</strong></p>
      <AliasTable rows={GEOCODE_COLUMNS} />
      <br/>
      <p>If any district assignment columns are specified, the processor will perform district assignment.</p>
      <br/>
      <p><strong style={{ color: 'teal' }}>District code columns</strong></p>
      <AliasTable rows={DISTRICT_COLUMNS} />
      <br/>
    </div>
  )
}

function AliasTable({ rows }) {
  return (
    <table className="columnAliasTable">
      <tbody>
      <tr>
        <th style={{ width: '110px' }}>Column</th>
        <th>Aliases</th>
      </tr>
      {rows.map(([ column, aliases ]) => (
        <tr key={column}>
          <td>{column}</td>
          <td><code>{aliases}</code></td>
        </tr>
      ))}
      </tbody>
    </table>
  )
}

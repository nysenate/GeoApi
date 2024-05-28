#!/bin/bash
source config.properties

shopt -s nullglob
# Loop used to prevent errors if there are no XLSX files.
# May need to run "apt-get install default-jre libreoffice-java-common"
for filename in "${non_txt_dir}"/*.xlsx; do
  libreoffice --headless --convert-to txt "${filename}" --outdir "${txt_dir}"
done

# NYC specific parsing
width=792
height=612
text_start_page=2
num_columns=3
single_col_width=$((width/num_columns))
header_height=42
footer_height=20
text_height=$((height - header_height - footer_height))

function getNycCounty() {
  filename=$1
  for county in "${nycCounties[@]}"; do
    if [[ "$filename" =~ ${county} ]]; then
      export nycCounty="${county}"
    fi
  done
}

for filename in "${non_txt_dir}"/*.pdf; do
  nycCounty=""
  getNycCounty "$filename"
  if [[ "$nycCounty" != "" ]]; then
    temp_output="${nycCounty}_temp.txt"
    perm_output="${txt_dir}/${nycCounty}.txt"
    rm "${perm_output}"
    for ((i=0; i<num_columns; i++)); do
      pdftotext -f ${text_start_page} -fixed 4 -x $((i * single_col_width)) -y ${header_height} -W ${single_col_width} -H ${text_height} "${filename}" "${temp_output}"
      cat "${temp_output}" >> "${perm_output}"
    done
    rm "${temp_output}"
  else pdftotext -layout "${filename}" "${txt_dir}/${filename%%.*}"
  fi
done

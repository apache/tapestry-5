# Copyright 2023 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# ## t5/core/graphviz
#
# Support to the core/Graphviz Tapestry component.
define ["https://cdn.jsdelivr.net/npm/@hpcc-js/wasm/dist/graphviz.umd.js"],
  (hpccWasm) ->
    render = (value, id, showDownloadLink) ->
      hpccWasm.Graphviz.load().then (graphviz) ->
        svg = graphviz.dot value
        div = document.getElementById id
        layout = graphviz.layout(value, "svg", "dot")
        div.innerHTML = layout
        if showDownloadLink
          link = document.getElementById (id + "-download")
          link.setAttribute "href", "data:image/svg+xml;charset=utf-8," + encodeURIComponent(layout)
    return render

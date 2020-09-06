// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app5.services

import org.apache.tapestry5.SymbolConstants
import org.apache.tapestry5.commons.MappedConfiguration
import org.apache.tapestry5.commons.Resource
import org.apache.tapestry5.func.F
import org.apache.tapestry5.http.services.Response
import org.apache.tapestry5.integration.app5.Client
import org.apache.tapestry5.integration.app5.ClientTracker
import org.apache.tapestry5.internal.services.assets.DelegatingSRS
import org.apache.tapestry5.ioc.annotations.Decorate
import org.apache.tapestry5.model.ComponentModel
import org.apache.tapestry5.services.ApplicationStateManager
import org.apache.tapestry5.services.assets.*
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer
import org.apache.tapestry5.services.pageload.ComponentResourceLocator
import org.apache.tapestry5.services.pageload.ComponentResourceSelector

class AppModule {

    void contributeApplicationDefaults(MappedConfiguration conf) {
        conf.add(SymbolConstants.PRODUCTION_MODE, true)
        conf.add(SymbolConstants.SUPPORTED_LOCALES, "en,fr")
        // Override to test TAP5-2361
        conf.add(SymbolConstants.BOOTSTRAP_ROOT, "context:bootstrap")
    }

    def decorateComponentRequestSelectorAnalyzer(ComponentRequestSelectorAnalyzer delegate, ApplicationStateManager mgr) {
        return {
            def selector = delegate.buildSelectorForRequest()
            def tracker = mgr.getIfExists(ClientTracker.class)

            tracker == null ? selector : selector.withAxis(Client.class, tracker.client)
        } as ComponentRequestSelectorAnalyzer
    }

    def decorateComponentResourceLocator(ComponentResourceLocator delegate) {
        return new ComponentResourceLocator() {

            Resource locateTemplate(ComponentModel model, ComponentResourceSelector selector) {
                def client = selector.getAxis(Client.class)

                if (client != null) {

                    // See if there's a copy of the file in a per-client sub-folder

                    def className = model.componentClassName
                    def simpleName = className.substring(className.lastIndexOf('.') + 1)

                    def skinned = model.baseResource.forFile("per-client/${client.name()}/${simpleName}.tml").forLocale(selector.locale)

                    if (skinned != null) return skinned
                }

                delegate.locateTemplate model, selector
            }

            List<Resource> locateMessageCatalog(Resource baseResource, ComponentResourceSelector selector) {

                def client = selector.getAxis(Client.class)

                if (client != null) {

                    def skinnedBase = baseResource.forFile("per-client/${client.name()}/${baseResource.file}")

                    def skinned = F.flow(delegate.locateMessageCatalog(skinnedBase, selector))
                    def standard = F.flow(delegate.locateMessageCatalog(baseResource, selector))

                    return skinned.interleave(standard).toList()
                }

                delegate.locateMessageCatalog(baseResource, selector);
            }
        }
    }

    @Decorate(id = "XRobots", serviceInterface = StreamableResourceSource.class)
    def enableXRobotsHeader(StreamableResourceSource delegate) {

        return new DelegatingSRS(delegate) {

            @Override
            StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException {

                def streamable = delegate.getStreamableResource(baseResource, processing, dependencies)

                if (streamable == null) {
                    return streamable
                }
                else {
                    return streamable.addResponseCustomizer(new ResponseCustomizer() {

                        @Override
                        void customizeResponse(StreamableResource resource, Response response) throws IOException {
                            response.setHeader("X-Robots-Tag", "noindex")
                        }
                    })
                }

            }
        }
    }
}

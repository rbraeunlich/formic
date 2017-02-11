#!/usr/bin/env bash
FORMIC_SERVER="http://10.200.1.67:80"
JAVA_OPTS="-DformicServer=$FORMIC_SERVER"
export JAVA_OPTS

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearInsertPreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearInsertCreationSimulation

./executeGatlingTest.sh 0 aef49a6b-4a63-44f4-b2d3-2b82a09e325f b256d765-bd01-4dd8-b1cb-8f54d9553bb4 a1c9e9ab-7b9f-4e4c-aa02-06918db0bb10 f16c15c8-4805-48d4-ad9e-bbadcddb1c5f
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearInsertCreationSimulation
./executeGatlingTest.sh 1
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearInsertCreationSimulation
./executeGatlingTest.sh 2 2fd66ec1-eede-40fd-93d3-48ab814cf7da a49aa34f-a5ae-4001-a755-a17cbde11f8e 3e94fb3e-35de-4e78-94d4-de98726a1d47 2b3d461e-1def-4984-b05f-9d61150ebd32
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearInsertCreationSimulation
./executeGatlingTest.sh 4 248e7e77-8082-4d21-90c8-8c8d9d95edac 47a50ee1-12a2-4471-897e-0d7678f6520b e181cc36-2c8a-46cf-99cc-f6ab37368596 d40307a9-c274-480c-97c8-a508f43960bc


gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearDeletePreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteSimulation" 0 6e849d89-0257-4b54-a096-162e7cb1a391 5705f28e-2403-4105-9ef8-86162a14a31f 91276f1b-0637-4178-9637-f251ffcec8d0 2802e58e-d89e-464e-af5c-6fca97bb138a
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteSimulation" 1 475369cd-ea6a-45b7-8ad1-ea8cb869b606 f4b897fb-fbf4-428f-9529-90904dddb625 f1d7953e-6fc4-4b45-a3d2-c95454d1452d 6012fc65-ddb8-4596-b9b6-4bf7b97051ea
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteSimulation" 2 7c881d32-1d6a-4ac9-815e-a1c8ccd027a5 b80039a9-44ba-447b-b193-b8cfb25c9aea be617d52-a678-4328-bcdf-31d63a8ade83 d98683b4-98ad-4489-a1fe-8a1cc9fc726f
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.linear.LinearDeleteSimulation" 4 94b9197f-d8c4-4ce5-bf01-f1c426375ef2 387a7fda-2ba7-4fa7-8644-0eddccac692a ec5ac72f-49b1-4194-a023-fbe38bb9a7dc b3a58960-f4df-4e79-9a27-d845bdfc95ba

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeInsertPreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeInsertCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeInsertSimulation" 0 966738e6-7cc9-4f30-a49e-24d74a05ccad ba16049b-3801-4353-a23c-cbc552f27c1f eb757090-1d20-4ade-9007-9e539c892fea f93ad468-3b35-4522-9590-8415774712a3
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeInsertSimulation" 1 e614932c-f6ff-4849-8ecf-14ed59f246aa dc2440ca-87d9-43e5-95d4-b65ae311a7bb 8d54e71d-b713-4faa-a579-3c47e616a416 af7d236b-3aa2-43a4-b247-f5dfa4871cf9
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeInsertSimulation" 2 9f0a78ea-9b97-404f-b4ba-da2b16fc4a51 efbc6b5b-80a6-4ca8-b2eb-f50d0227d9fb aa819059-2dcd-4ec3-b22c-53cf543bafe6 22ef9fd2-5ab2-43ec-a23e-417575d6c40a
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeInsertSimulation" 4 1d166b36-0dad-45ca-860c-f8045f0d8d94 4846c5b0-9707-4e00-a7ad-de0b2c127427 526f70e9-d442-4037-a2b5-859659573ff4 93401b07-252b-431a-abfd-d13d74726b6f

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeDeletePreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteSimulation" 0 62cb2fc2-f360-4630-b1ff-8dbeaf94a011 e02f3956-777e-43ab-b820-2f67ed9d1e54 3e76d2ea-eb6b-477c-ba44-29bb01d5b8cd a2e0b00e-b2c6-4661-a82b-76827e2c2e33
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteSimulation" 1 e4e557a8-cbc5-49d9-bdd0-d2f04db7401d 32ab52e2-3702-4b15-b51b-6a02ae5d2689 a56683c1-f45f-49c1-9172-b74203fbd33b 2dd651a1-69c0-42df-96af-e93d7b75fb3c
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteSimulation" 2 61f78f87-92a2-4d98-aadb-8696dcf6c15d f87eee42-4729-4406-9c60-9ddb68fbd865 5783aeeb-fdb3-4fa9-8858-b3544bbe05a8 ee0c49df-04ce-4597-a621-d8049648e219
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.tree.TreeDeleteSimulation" 4 c0487fc3-7f61-4d5d-9834-b9df573dff6a 5ee530a1-fec1-4667-9813-a40ec38fead7 c30ac362-a146-4f0b-a3f7-92a762a6521f 61a110ac-c1e7-4c0e-843d-c7a2f8f2ddd3

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonInsertPreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonInsertCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonInsertSimulation" 0 0ef689b3-618a-4bcc-a045-b50cc6ae0f13 43d465b6-1405-443a-9009-1f1257b57c19 ba7a2bd5-239b-458c-a937-74728880b60a 29b0eb86-9ba2-4672-8857-c070816212df
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonInsertSimulation" 1 09c15089-f2f9-4af5-870f-bfe7e8c5a807 fdfcf034-46c2-4799-99d6-c4a4bdc03265 9c7d3033-e63a-417e-8608-cb73fe6e50e2 0a7261cb-dc0b-4183-a91f-8afd3958e4da
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonInsertSimulation" 2 b4322f50-bbd7-4146-a23c-9b67205b3b47 6b972e6d-a1f1-426b-b9f8-28e4cfddd20c 25f900d8-3742-459c-b068-24b249dd4ff2 428afbfb-eea3-4b4a-86c5-bd146c48e891
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonInsertCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonInsertSimulation" 4 81081638-dae4-4cf2-b2c3-d04f678f2b71 16623346-1697-4cf5-952e-069b48ca11a5 8c1c60a7-7716-4e97-a777-7456fd02096e e39824f1-3cf8-48c4-ab4e-ef40f4e5b8e9

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonDeletePreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonDeleteCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonDeleteSimulation" 0 89cd7275-0949-4756-98f5-1463377b52bb c82e06c6-2f25-4d55-afab-9cf920643c53 0df5b481-41ce-4f85-ad02-14bd5518f6c6 de636d20-1cdf-4697-9bbc-be5c96b6d4c2
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonDeleteSimulation" 1 d8974411-5bf2-4bbc-838e-aa31fbd27217 8fab2f32-38f4-46b2-b758-783dbe22725d 1de63211-04ee-4dbe-b737-a04996da3297 d31f67ab-bda4-4aca-82cc-f22f75ad147b
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonDeleteSimulation" 2 e1727206-54b9-491f-b879-32e753869e82 c20380e7-c601-4b59-aa5e-7bc8a0d5d7b5 eb2587f7-b401-41ae-b88b-8ba091781eca 598fcf91-55a8-4c54-b50f-6c310f212d51
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonDeleteCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonDeleteSimulation" 4 67225cd0-7f0f-43e5-8a4b-4039367b03dd 60fd9d7d-ca19-41eb-9e5b-b179a6ac3150 734526b2-cf2e-485a-b300-f72114f26823 da3aa2aa-f075-4b47-a8d5-f07566e54cc6

gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonReplacePreparationSimulation
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonReplaceCreationSimulation

./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonReplaceSimulation" 0 d21e30eb-40b5-4152-b6e6-c51f0d1669a6 1ef7df3d-e511-4b64-a30c-8a2b57ad870a de68e77f-06b5-43f8-b3e6-0844ff6f9e5e a420cfb9-cbe7-4bec-987c-9fc76534ff3a
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonReplaceCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonReplaceSimulation" 1 0d829fbc-9500-4242-b930-0eb8bf069990 ad9d967d-c434-482f-b7e2-2f1552c25d11 1f9bcf7f-6ede-4eb3-aa9b-99dfb07b76fc 47713049-9efd-4a4a-b050-927fd22b4ff0
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonReplaceCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonReplaceSimulation" 2 c47f3d21-e17b-411f-909d-53f4118eee91 8daeee9f-ba4c-484c-aa52-eb2dee88cb59 723255d4-6482-4d16-aa80-0a1ed18af41f a2ce6ff6-644e-40fa-98ba-5642d1a0cf8f
gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.json.JsonReplaceCreationSimulation
./executeGatlingTest.sh "de.tu_berlin.formic.gatling.experiment.json.JsonReplaceSimulation" 4 71191165-dc1f-4520-8ee6-ebb1d9fb549d b2fdc194-e325-424b-aede-6269611334d9 bbba1f8b-f203-441a-ac3d-e2aff7d0ef77 fe2d1f60-3bc9-4950-be38-12a5398b1f80
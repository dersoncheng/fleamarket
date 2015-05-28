import os

# project path
project_path = "/Users/Annie/phoenix2/packages/phoenix"
# sdk tools path
sdk_tools_path = "/Applications/Android Studio.app/sdk/tools"

def runLint(adb_path):
    os.chdir(adb_path)
     
    gfxinfo_str = os.popen("lint --check UnusedResources " + project_path)

    return doClean(gfxinfo_str.read())

def doClean(message):
    lint_items = message.split('\n');
    cleaned_count = 0;

    # find unused file line by line
    for lint_item in lint_items:
        if len(lint_item) > 0 and lint_item.find("UnusedResources")!=-1:
            if(lint_item.startswith("res/drawable") | lint_item.startswith("res-sync/drawable")
                | lint_item.startswith("res-wan/drawable") | lint_item.startswith("res-xibaibai/drawable")):
                deleteFile(lint_item[ 0 : lint_item.index(':')])
                cleaned_count += 1;
            elif(lint_item.startswith("res/menu") | lint_item.startswith("res-sync/menu")
                | lint_item.startswith("res-wan/menu") | lint_item.startswith("res-xibaibai/menu")):
                deleteFile(lint_item[ 0 : lint_item.index(':')])
                cleaned_count += 1;
            elif(lint_item.startswith("res/layout") | lint_item.startswith("res-sync/layout")
                | lint_item.startswith("res-wan/layout") | lint_item.startswith("res-xibaibai/layout")):
                deleteFile(lint_item[ 0 : lint_item.index(':')])
                cleaned_count += 1;
            elif(lint_item.startswith("res/xml") | lint_item.startswith("res-sync/xml")
                | lint_item.startswith("res-wan/xml") | lint_item.startswith("res-xibaibai/xml")):
                deleteFile(lint_item[ 0 : lint_item.index(':')])
                cleaned_count += 1;
            elif(lint_item.startswith("res/color") | lint_item.startswith("res-sync/color")
                | lint_item.startswith("res-wan/color") | lint_item.startswith("res-xibaibai/color")):
                deleteFile(lint_item[ 0 : lint_item.index(':')])
                cleaned_count += 1;
            # else:
                # print "didn't clear and lint_item is: ", lint_item
    return cleaned_count;        

def deleteFile(relative):
    path = project_path + "/" + relative
    print "deleteFile: ", path
    if os.path.exists(path):
        try:
            os.remove(path)
        except:
            pass
    
def main():
    print "begin clear"
    while (runLint(sdk_tools_path) > 0):
        print "clean once"

main()
